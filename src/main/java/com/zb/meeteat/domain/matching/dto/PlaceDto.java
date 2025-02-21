package com.zb.meeteat.domain.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PlaceDto {

  private Long id;
  private Long kakaomapsId;
  private String name;
  private String category_name;
  private String road_address_name;
  private String phone;
  private Double lon;
  private Double lat;
  private Double rating;
}
