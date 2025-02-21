package com.zb.meeteat.domain.restaurant.repository;

import com.zb.meeteat.domain.restaurant.dto.RestaurantReviewsResponse;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import com.zb.meeteat.domain.restaurant.entity.RestaurantReview;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Long>, RestaurantReviewRepositoryCustom {

  RestaurantReview findRestaurantReviewByMatchingHistoryId(Long matchingHistoryId);

  RestaurantReview findTop1ByRestaurantOrderByImgUrlDesc(Restaurant restaurant);

  List<RestaurantReview> findAllByRestaurant(Restaurant restaurant);
}
