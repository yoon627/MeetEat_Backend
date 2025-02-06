package com.zb.meeteat.domain.restaurant.repository;

import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

  // 지역, 카테고리명, 장소명
  @Query("SELECT r FROM Restaurant r "
      + "WHERE r.road_address_name LIKE %:region% "
      + "AND r.place_name LIKE %:placeName% "
      + "AND r.category_name LIKE %:categoryName% ")
  Page<Restaurant> findRestaurantByRegionAndCategoryNameAndPlaceName(
      String region, String placeName, String categoryName, Pageable pageable);

  // 지역, 장소명, 카테고리명, 거리순-오름차순 정렬
  @Query("SELECT r " +
      "FROM Restaurant r WHERE r.road_address_name LIKE %:region% " +
      "AND r.place_name LIKE %:placeName% " +
      "AND r.category_name LIKE %:categoryName% " +
      "ORDER BY "
      + "(6371 * acos(cos(radians(:userLat)) * cos(radians(r.y)) * cos(radians(r.x) - radians(:userLon)) + sin(radians(:userLat)) * sin(radians(r.y)))) ASC")
  Page<Restaurant> findRestaurantByResionAndPlaceNameAndCateoryNameOrderByDistance(
      @Param("userLat") double y,
      @Param("userLon") double x,
      @Param("region") String region,
      @Param("placeName") String placeName,
      @Param("categoryName") String categoryName,
      Pageable pageable);

  // 지역, 장소명, 카테고리명, 평점순-내림차순 정렬
  @Query("SELECT r FROM Restaurant r "
      + "WHERE r.road_address_name LIKE %:region% "
      + "AND r.place_name LIKE %:placeName% "
      + "AND r.category_name LIKE %:categoryName% ")
  Page<Restaurant> findRestaurantByRegionAndPlaceNameAndCategoryNameOrderByRatingDesc(
      String region, String placeName, String categoryName, Pageable pageable);
}
