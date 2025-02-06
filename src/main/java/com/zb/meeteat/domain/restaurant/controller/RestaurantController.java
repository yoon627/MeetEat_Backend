package com.zb.meeteat.domain.restaurant.controller;

import com.zb.meeteat.domain.restaurant.dto.SearchRequest;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import com.zb.meeteat.domain.restaurant.entity.RestaurantReview;
import com.zb.meeteat.domain.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  // 식당 상세 조회
  @GetMapping("/{restaurantId}")
  public ResponseEntity<Restaurant> get(
      @PathVariable(name = "restaurantId") Long restaurantId) {

    Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
    if (restaurant == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(restaurant);
  }

  // 식당 리뷰 조회
  @GetMapping("/{restaurantId}/reviews")
  public ResponseEntity<Page<RestaurantReview>> getReviews(
      @PathVariable(name = "restaurantId") Long restaurantId,
      @RequestParam(name = "page", defaultValue = "1") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {

    return ResponseEntity.ok(
        restaurantService.getRestaurantReviews(restaurantId, page, size));
  }
}
