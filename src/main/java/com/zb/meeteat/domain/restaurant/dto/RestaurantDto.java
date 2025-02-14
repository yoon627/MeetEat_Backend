package com.zb.meeteat.domain.restaurant.dto;

import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RestaurantDto {

  private Long id;
  private Long kakaomapsId;
  private String placeName;
  private String categoryName;
  private String roadAddressName;
  private String phone;
  private Double lon;
  private Double lat;
  private Double rating;


  public static Restaurant toEntity(RestaurantDto restaurantDto) {
    return Restaurant.builder()
        .id(restaurantDto.getId())
        .kakaomapsId(restaurantDto.getKakaomapsId())
        .placeName(restaurantDto.getPlaceName())
        .phone(restaurantDto.getPhone())
        .x(restaurantDto.getLon())
        .y(restaurantDto.getLat())
        .roadAddressName(restaurantDto.getRoadAddressName())
        .categoryName(restaurantDto.getCategoryName())
        .rating(restaurantDto.getRating()).build();
  }
}
