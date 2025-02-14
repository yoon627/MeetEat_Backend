package com.zb.meeteat.domain.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.stream.Stream;

public enum Sort {
  DEFAUT,
  RATING, // 오름차순
  DISTANCE; // 내림차순


  @JsonCreator
  public static Sort fromString(String text) {
    return Stream.of(Sort.values())
        .filter(sort -> sort.toString().equals(text))
        .findFirst()
        .orElse(null);
  }
}