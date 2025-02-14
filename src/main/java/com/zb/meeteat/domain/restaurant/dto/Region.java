package com.zb.meeteat.domain.restaurant.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.stream.Stream;

public enum Region {
  서울, 부산, 대구, 인천, 광주, 대전, 울산, 세종, 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주;

  @JsonCreator
  public static Region fromString(String text) {
    return Stream.of(Region.values())
        .filter(region -> region.toString().equals(text))
        .findFirst()
        .orElse(null);
  }
}
