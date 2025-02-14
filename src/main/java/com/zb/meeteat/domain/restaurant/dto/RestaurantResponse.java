package com.zb.meeteat.domain.restaurant.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {

  private Long id;
  private Long kakaomaps_id;
  private String place_name;
  private String phone;
  private double y;
  private double x;
  private String road_address_name;
  private String category_name;
  private double rating;
  private String thumbnail;
}
