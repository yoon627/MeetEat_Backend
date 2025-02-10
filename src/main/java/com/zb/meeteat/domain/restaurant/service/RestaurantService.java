package com.zb.meeteat.domain.restaurant.service;

import com.zb.meeteat.domain.matching.entity.Matching;
import com.zb.meeteat.domain.matching.entity.MatchingHistory;
import com.zb.meeteat.domain.matching.entity.MatchingStatus;
import com.zb.meeteat.domain.matching.repository.MatchingHistoryRepository;
import com.zb.meeteat.domain.restaurant.dto.CreateReviewRequest;
import com.zb.meeteat.domain.restaurant.dto.SearchRequest;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RestaurantService {

  private final RestaurantRepository restaurantRepository;
  private final RestaurantReviewRepository restaurantReviewRepository;
  private final UserRepository userRepository;
  private final MatchingHistoryRepository matchingHistoryRepository;
  private static int MAX_FILE_COUNT = 7;

  public Page<Restaurant> getRestaurantList(SearchRequest search) {

    String categoryName = search.getCategoryName().equals("전체") ? "" : search.getCategoryName();
    search.setCategoryName(categoryName);

    // Pageable 객체 생성 (페이지, 사이즈, 정렬 방식)
    Pageable pageable = PageRequest.of(search.getPage() - 1, search.getSize());

    // 정렬 기준에 따라 쿼리 메소드 실행
    if ("RATING".equals(search.getSorted())) {
      if (Double.isNaN(search.getUserX()) || Double.isNaN(search.getUserX())) {
        throw new CustomException(ErrorCode.BAD_REQUEST);
      }

      return restaurantRepository.findRestaurantByRegionAndPlaceNameAndCategoryNameOrderByRatingDesc(
          search.getRegion(),
          search.getPlaceName(),
          search.getCategoryName(),
          pageable);
    } else if ("DISTANCE".equals(search.getSorted())) {
      return restaurantRepository.findRestaurantByResionAndPlaceNameAndCateoryNameOrderByDistance(
          search.getUserY(),
          search.getUserX(),
          search.getRegion(),
          search.getPlaceName(),
          search.getCategoryName(),
          pageable);
    } else {
      return restaurantRepository.findRestaurantByRegionAndCategoryNameAndPlaceName(
          search.getRegion(),
          search.getPlaceName(),
          search.getCategoryName(),
          pageable);
    }
  }

  public Restaurant getRestaurant(Long restaurantId) {
    return restaurantRepository.findById(restaurantId)
        .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
  }

  public Page<RestaurantReview> getRestaurantReviews(
      Long restaurantId, int page, int size) {

    // Pageable 객체 생성 (페이지, 사이즈, 정렬 방식)
    Pageable pageable = PageRequest.of(page - 1, size);

    return restaurantReviewRepository.findRestaurantReviewByRestaurantId(restaurantId, pageable);
  }

  public RestaurantReview createReview(Long userId, CreateReviewRequest req) throws CustomException {

    // 1. user 확인
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
    if (duration.toHours() < 2) {
      throw new CustomException(ErrorCode.REVIEW_TIME_NOT_EXCEEDED);
    }

    Restaurant restaurant = matching.getRestaurant();
    if (restaurant == null) {
      throw new CustomException(ErrorCode.NOT_EXIST_RESTAURANT);
    }

    // 4. 첨부파일 확인
    String imageUrls = saveImage(req.getFile());

    // 5. 데이터 저장하기
    RestaurantReview review = RestaurantReview.builder()
        .rating(req.getRating())
        .description(req.getDescription())
        .imgUrl(imageUrls)
        .matchingHistoryId(history.getId())
        .user(user)
        .restaurant(restaurant)
        .build();

    return restaurantReviewRepository.save(review);
  }

  public RestaurantReview getMyReviewByMatching(Long matchingHistoryId) {
    MatchingHistory history = matchingHistoryRepository.findById(matchingHistoryId)
        .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

    Matching matching = history.getMatching();
    if (history.getStatus().equals(MatchingStatus.CANCELED) ||
        matching.getStatus().equals(MatchingStatus.CANCELED)) {
      throw new CustomException(ErrorCode.CANCELED_MATCHING);
    }

    return restaurantReviewRepository.findRestaurantReviewByMatchingHistoryId(matchingHistoryId);
  }

  private String saveImage(MultipartFile file) {
    if (file.isEmpty() && file.getSize() > MAX_FILE_COUNT) {
      return null;
    }

    // todo 이미지 저장하기
    String imagesUrl = null;
      if (file.getSize() > 0) {

        // 파일 확장자 확인
        validateFileExtension(file);
        // AWS image Upload => imageUrls = [imageUrl]
      }

    return imagesUrl;
  }

  private void validateFileExtension(MultipartFile file) {
    List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "raw", "heic", "heif");

    String fileName = file.getOriginalFilename();
    String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }
  }
}
