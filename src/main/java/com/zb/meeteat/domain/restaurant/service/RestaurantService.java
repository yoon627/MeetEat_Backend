package com.zb.meeteat.domain.restaurant.service;

import com.zb.meeteat.domain.matching.entity.Matching;
import com.zb.meeteat.domain.matching.entity.MatchingHistory;
import com.zb.meeteat.domain.matching.entity.MatchingStatus;
import com.zb.meeteat.domain.matching.repository.MatchingHistoryRepository;
import com.zb.meeteat.domain.restaurant.dto.Category;
import com.zb.meeteat.domain.restaurant.dto.CreateReviewRequest;
import com.zb.meeteat.domain.restaurant.dto.RestaurantDto;
import com.zb.meeteat.domain.restaurant.dto.RestaurantMyReviewResponse;
import com.zb.meeteat.domain.restaurant.dto.RestaurantResponse;
import com.zb.meeteat.domain.restaurant.dto.RestaurantReviewsResponse;
import com.zb.meeteat.domain.restaurant.dto.SearchRequest;
import com.zb.meeteat.domain.restaurant.dto.Sort;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import com.zb.meeteat.domain.restaurant.entity.RestaurantReview;
import com.zb.meeteat.domain.restaurant.repository.RestaurantRepository;
import com.zb.meeteat.domain.restaurant.repository.RestaurantReviewRepository;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.exception.CustomException;
import com.zb.meeteat.exception.ErrorCode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

  private static int MAX_FILE_COUNT = 7;
  private static int AFTER_MATCHING_TIME = 2;
  private final S3ImageUpload s3ImageUpload;
  private final RestaurantRepository restaurantRepository;
  private final RestaurantReviewRepository restaurantReviewRepository;
  private final UserRepository userRepository;
  private final MatchingHistoryRepository matchingHistoryRepository;

  public Restaurant saveRestaurant(RestaurantDto restaurantDto) {
    Restaurant restaurant = restaurantRepository.findByPlaceNameAndRoadAddressName(
        restaurantDto.getName(), restaurantDto.getRoad_address_name());
    if (restaurant != null) {
      return restaurant;
    } else {
      return restaurantRepository.save(RestaurantDto.toEntity(restaurantDto));
    }
  }

  public Page<RestaurantResponse> getRestaurantList(SearchRequest search) {
    String categoryName =
        Category.전체.equals(search.getCategoryName()) ? "" : search.getCategoryName().toString();

    // Pageable 객체 생성 (페이지, 사이즈, 정렬 방식)
    Pageable pageable = PageRequest.of(search.getPage(), search.getSize());

    // 정렬 기준에 따라 쿼리 메소드 실행
    Page<RestaurantResponse> restaurants = Page.empty();

    if (Sort.RATING.equals(search.getSorted())) {
      restaurants = restaurantRepository.getRestaurantByRegionAndPlaceNameAndCategoryNameOrderByRatingDesc(
          search.getRegion().toString(),
          search.getPlaceName(),
          categoryName,
          pageable);
    } else if (Sort.DISTANCE.equals(search.getSorted())) {
      if (Double.isNaN(search.getUserX()) || Double.isNaN(search.getUserX())) {
        throw new CustomException(ErrorCode.USER_LOCATION_NOT_PROVIDED);
      }

      restaurants = restaurantRepository.getRestaurantByRegionAndPlaceNameAndCategoryNameOrderByDistance(
          search.getUserY(),
          search.getUserX(),
          search.getRegion().toString(),
          search.getPlaceName(),
          categoryName,
          pageable);
    } else {
      restaurants = restaurantRepository.getRestaurantByRegionAndCategoryNameAndPlaceName(
          search.getRegion().toString(),
          search.getPlaceName(),
          categoryName,
          pageable);
    }

    return restaurants;
  }

  public RestaurantResponse getRestaurant(Long restaurantId) {

    Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);

    if (restaurant == null) {
      return new RestaurantResponse();
    }

    // 리뷰이미지가 있는 최신리뷰 가지고 오기
    RestaurantReview lastedReview = restaurantReviewRepository.findTop1ByRestaurantOrderByImgUrlDesc(
        restaurant);
    String imgUrl = "";
    if (lastedReview != null) {
      imgUrl = lastedReview.getImgUrl();
    }

    return RestaurantResponse.fromRestaurant(restaurant, imgUrl);
  }

  public Page<RestaurantReviewsResponse> getRestaurantReviews(
      Long restaurantId, int page, int size) {

    // Pageable 객체 생성 (페이지, 사이즈, 정렬 방식)
    Pageable pageable = PageRequest.of(page, size);

    return restaurantReviewRepository.getRestaurantReviewByRestaurantId(restaurantId, pageable);
  }

  @Transactional
  public void createReview(Long userId, CreateReviewRequest req)
      throws CustomException {

    // 1. user 정보 가져오기
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

    // 2. 매칭 참석 여부 확인
    MatchingHistory history = matchingHistoryRepository.findById(req.getMatchingHistoryId())
        .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

    Matching matching = history.getMatching();
    if (matching.getStatus().equals(MatchingStatus.CANCELED)
        || history.getStatus().equals(MatchingStatus.CANCELED)) {
      throw new CustomException(ErrorCode.REVIEW_NOT_ALLOWED_FOR_CANCELED_MATCHING);
    }

    // 3. 후기 작성 시간 확인 (매칭 약속시간 이후 2시간)
    LocalDateTime matchingTime = matching.getCreatedAt();
    LocalDateTime currentTime = LocalDateTime.now();
    Duration duration = Duration.between(matchingTime, currentTime);
    if (duration.toHours() < AFTER_MATCHING_TIME) {
      throw new CustomException(ErrorCode.REVIEW_TIME_NOT_EXCEEDED);
    }

    // 4. 첨부파일 확인
    String imageUrls = saveImage(req.getFiles());

    // 5. 데이터 저장하기
    RestaurantReview review = RestaurantReview.builder()
        .rating(req.getRating())
        .description(req.getDescription())
        .imgUrl(imageUrls)
        .matchingHistoryId(req.getMatchingHistoryId())
        .user(user)
        .restaurant(matching.getRestaurant())
        .build();

    restaurantReviewRepository.save(review);

    // 6. 식당 평점 변경
    updateRestaurantRating(matching.getRestaurant());
  }

  public RestaurantMyReviewResponse getMyReviewByMatching(Long matchingHistoryId, Long userId) {
    MatchingHistory history = matchingHistoryRepository.findById(matchingHistoryId)
        .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

    if (!userId.equals(history.getUserId())) {
      throw new CustomException(ErrorCode.BAD_REQUEST);
    }

    Matching matching = history.getMatching();
    if (history.getStatus().equals(MatchingStatus.CANCELED) ||
        matching.getStatus().equals(MatchingStatus.CANCELED)) {
      throw new CustomException(ErrorCode.CANCELED_MATCHING);
    }

    RestaurantReview myReview = restaurantReviewRepository.findRestaurantReviewByMatchingHistoryId(
        matchingHistoryId);

    if (myReview == null) {
      return new RestaurantMyReviewResponse();
    }

    return RestaurantMyReviewResponse.builder()
        .id(myReview.getId())
        .rating(myReview.getRating())
        .description(myReview.getDescription())
        .imageUrl(myReview.getImgUrl())
        .nickName(myReview.getUser().getNickname())
        .createdAt(myReview.getCreatedAt())
        .build();
  }

  private String saveImage(MultipartFile[] files) {
    if (files.length > MAX_FILE_COUNT || files.length < 1
        || files[0].isEmpty() || Objects.requireNonNull(files[0].getOriginalFilename()).isEmpty()) {
      return null;
    }

    List<String> imgUrlList = new ArrayList<String>();
    for (MultipartFile image : files) {
      String newFileName = s3ImageUpload.uploadImage(image);
      imgUrlList.add(newFileName);
      log.info("➡️ 이미지 저장 :{} - {}", image.getOriginalFilename(), newFileName);
    }

    return String.join(",", imgUrlList);
  }

  private void updateRestaurantRating(Restaurant restaurant) {
    List<RestaurantReview> reviews = restaurantReviewRepository.findAllByRestaurant(restaurant);
    Double avgRating = reviews.stream()
        .mapToInt(RestaurantReview::getRating)
        .average()
        .orElse(0.0);

    restaurant.setRating(avgRating);
    restaurantRepository.save(restaurant);
  }

}
