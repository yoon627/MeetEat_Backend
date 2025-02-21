package com.zb.meeteat.domain.restaurant.dto;

import com.zb.meeteat.domain.matching.dto.PlaceDto;
import com.zb.meeteat.domain.matching.dto.UserMatchingHistoryDto;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@Builder
public class RestaurantDto {

  private Long id;
  private Long kakaomapsId;
  private String name;
  private String category_name;
  private String road_address_name;
  private String phone;
  private Double lon;
  private Double lat;
  private Double rating;
  private PlaceDto place;
  private UserMatchingHistoryDto user;


  public static Restaurant toEntity(RestaurantDto restaurantDto) {
    log.info("RestautrantDto.toEntity - restaurant Id: " + restaurantDto.getId());
    return Restaurant.builder()
        .kakaomapsId(restaurantDto.getKakaomapsId())
        .placeName(restaurantDto.getName())
        .phone(restaurantDto.getPhone())
        .x(restaurantDto.getLon())
        .y(restaurantDto.getLat())
        .roadAddressName(restaurantDto.getRoad_address_name())
        .categoryName(restaurantDto.getCategory_name())
        .rating(restaurantDto.getRating())
        .build();
  }

  public static RestaurantDto toDto(Restaurant restaurant) {
    return RestaurantDto.builder()
        .id(restaurant.getId())
        .kakaomapsId(restaurant.getKakaomapsId())
        .name(restaurant.getPlaceName())
        .category_name(restaurant.getCategoryName())
        .road_address_name(restaurant.getRoadAddressName())
        .phone(restaurant.getPhone())
        .lon(restaurant.getX())
        .lat(restaurant.getY())
        .phone(restaurant.getPhone())
        .rating(restaurant.getRating())
        .build();
  }

  public static PlaceDto toPlaceDto(RestaurantDto restaurantDto) {
    return PlaceDto.builder()
        .id(restaurantDto.getId())
        .kakaomapsId(restaurantDto.getKakaomapsId())
        .name(restaurantDto.getName())
        .category_name(restaurantDto.getCategory_name())
        .road_address_name(restaurantDto.getRoad_address_name())
        .phone(restaurantDto.getPhone())
        .lon(restaurantDto.getLon())
        .lat(restaurantDto.getLat())
        .rating(restaurantDto.getRating())
        .build();
  }
}
