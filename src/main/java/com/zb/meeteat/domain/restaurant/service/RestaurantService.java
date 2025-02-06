package com.zb.meeteat.domain.restaurant.service;

import com.zb.meeteat.domain.restaurant.dto.SearchRequest;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import com.zb.meeteat.domain.restaurant.repository.RestaurantRepository;
import com.zb.meeteat.exception.CustomException;
import com.zb.meeteat.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantService {

  private final RestaurantRepository restaurantRepository;

  public Page<Restaurant> getRestaurantList(SearchRequest search) {

    // Pageable 객체 생성 (페이지, 사이즈, 정렬 방식)
    Pageable pageable = PageRequest.of(search.getPage() - 1, search.getSize());

    // 정렬 기준에 따라 쿼리 메소드 실행
    if ("RATING".equals(search.getSorted())) {
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

  // 식당 상세 조회
  public Restaurant getRestaurant(Long restaurantId) {
    return restaurantRepository.findById(restaurantId)
        .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
  }

}
