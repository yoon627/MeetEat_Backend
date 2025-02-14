package com.zb.meeteat.domain.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.stream.Stream;

public enum Category {
  전체, 한식, 중식, 일식, 양식;

  @JsonCreator
  public static Category fromString(String text) {
    return Stream.of(Category.values())
        .filter(category -> category.toString().equals(text))
        .findFirst()
        .orElse(null);
  }
}

