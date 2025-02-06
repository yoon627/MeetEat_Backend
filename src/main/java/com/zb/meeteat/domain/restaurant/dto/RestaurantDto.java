package com.zb.meeteat.domain.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RestaurantDto {

  long id;
  String name;
  String categoryName;
  String roadAddressName;
  String phone;
  double lon;
  double lat;
}
