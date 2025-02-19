package com.zb.meeteat.domain.restaurant.repository;

import com.zb.meeteat.domain.restaurant.dto.RestaurantReviewsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantReviewRepositoryCustom {
  Page<RestaurantReviewsResponse> findRestaurantReviewsByRestaurantId(Long restaurantId, Pageable pageable);
}
