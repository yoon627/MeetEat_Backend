package com.zb.meeteat.domain.restaurant.controller;

import com.zb.meeteat.domain.restaurant.dto.SearchRequest;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import com.zb.meeteat.domain.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurant")
public class RestaurantController {
  private final RestaurantService restaurantService;

  // 식당조회
  @PostMapping("/search")
  public ResponseEntity<Page<Restaurant>> searchRestaurant(
      @RequestBody @Valid SearchRequest search) {

    return ResponseEntity.ok(restaurantService.getRestaurantList(search));
  }
}
