package com.zb.meeteat.domain.restaurant.repository;

import com.zb.meeteat.domain.restaurant.entity.RestaurantReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Long> {

  @Query("SELECT r "
      + "FROM RestaurantReview r "
      + "WHERE r.restaurant.id = :restaurantId")
  Page<RestaurantReview> findRestaurantReviewByRestaurantId(
      @Param("restaurantId") Long restaurantId, Pageable pageable);

  RestaurantReview findRestaurantReviewByMatchingHistoryId(Long matchingHistoryId);
}
