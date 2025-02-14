package com.zb.meeteat.domain.restaurant.repository;

import com.zb.meeteat.domain.restaurant.dto.RestaurantResponse;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

  @Query(value = "SELECT r.id, r.kakaomaps_id, r.place_name, r.phone, r.y, r.x, r.road_address_name, "
      + "r.category_name, r.rating, COALESCE(MAX(NULLIF(rr.img_url, '')), '') as thumbnail "
      + "FROM Restaurant r LEFT JOIN RestaurantReview rr ON r.id = rr.restaurant_id "
      + "WHERE r.id = :restaurantId "
      + "GROUP BY r.id ORDER BY MAX(rr.id) DESC", nativeQuery = true)
  RestaurantResponse getRestaurantById(Long restaurantId);

  // 지역, 카테고리명, 장소명
  @Query(value = "SELECT r.id, r.kakaomaps_id, r.place_name, r.phone, r.y, r.x, r.road_address_name, "
      + "r.category_name, r.rating, COALESCE(MAX(NULLIF(rr.img_url, '')), '') as thumbnail "
      + "FROM Restaurant r LEFT JOIN RestaurantReview rr ON r.id = rr.restaurant_id "
      + "WHERE r.road_address_name LIKE %:region% "
      + "AND r.place_name LIKE %:placeName% "
      + "AND r.category_name LIKE %:categoryName% "
      + "GROUP BY r.id ORDER BY MAX(rr.id) DESC", nativeQuery = true)
  Page<RestaurantResponse> getRestaurantByRegionAndCategoryNameAndPlaceName(
      String region, String placeName, String categoryName, Pageable pageable);

  // 지역, 장소명, 카테고리명, 거리순-오름차순 정렬
  @Query(value = "SELECT r.id, r.kakaomaps_id, r.place_name, r.phone, r.y, r.x, r.road_address_name, "
      + "r.category_name, r.rating, COALESCE(MAX(NULLIF(rr.img_url, '')), '') as thumbnail "
      + "FROM Restaurant r LEFT JOIN RestaurantReview rr ON r.id = rr.restaurant_id "
      + "WHERE r.road_address_name LIKE %:region% "
      + "AND r.place_name LIKE %:placeName% "
      + "AND r.category_name LIKE %:categoryName% "
      + "GROUP BY r.id ORDER by "
      + "(6371 * acos(cos(radians(:userLat)) * cos(radians(r.y)) * cos(radians(r.x) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(r.y)))) ASC, MAX(rr.id) DESC"
  , nativeQuery = true)
  Page<RestaurantResponse> getRestaurantByRegionAndPlaceNameAndCategoryNameOrderByDistance(
      @Param("userLat") double y,
      @Param("userLon") double x,
      @Param("region") String region,
      @Param("placeName") String placeName,
      @Param("categoryName") String categoryName,
      Pageable pageable);

  // 지역, 장소명, 카테고리명, 평점순-내림차순 정렬
  @Query(value = "SELECT r.id, r.kakaomaps_id, r.place_name, r.phone, r.y, r.x, r.road_address_name, "
      + "r.category_name, r.rating, COALESCE(MAX(NULLIF(rr.img_url, '')), '') as thumbnail "
      + "FROM Restaurant r LEFT JOIN RestaurantReview rr ON r.id = rr.restaurant_id "
      + "WHERE r.road_address_name LIKE %:region% "
      + "AND r.place_name LIKE %:placeName% "
      + "AND r.category_name LIKE %:categoryName% "
      + "GROUP BY r.id ORDER by r.rating DESC", nativeQuery = true)
  Page<RestaurantResponse> getRestaurantByRegionAndPlaceNameAndCategoryNameOrderByRatingDesc(
      @Param("region") String region,
      @Param("placeName") String placeName,
      @Param("categoryName") String categoryName,
      Pageable pageable);
}
