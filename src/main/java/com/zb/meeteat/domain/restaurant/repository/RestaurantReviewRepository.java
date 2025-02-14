package com.zb.meeteat.domain.restaurant.repository;

import com.zb.meeteat.domain.restaurant.dto.RestaurantReviewsResponse;
import com.zb.meeteat.domain.restaurant.entity.RestaurantReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Long> {

  @Query(value = "SELECT r.id, r.rating, r.description, r.img_url as imgUrl, u.nickname, "
      + "DATE_FORMAT(r.created_at, '%Y-%m-%d %H:%i:%s') AS created_at "
      + "FROM RestaurantReview r "
      + "JOIN user u ON r.user_id = u.id "
      + "WHERE r.restaurant_id = :restaurantId ORDER BY r.id DESC"
      , nativeQuery = true)
  Page<RestaurantReviewsResponse> getRestaurantReviewByRestaurantId(
      @Param("restaurantId") Long restaurantId, Pageable pageable);

  RestaurantReview findRestaurantReviewByMatchingHistoryId(Long matchingHistoryId);
}
