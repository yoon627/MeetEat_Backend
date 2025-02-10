package com.zb.meeteat.domain.restaurant.controller;

import com.zb.meeteat.domain.restaurant.dto.CreateReviewRequest;
import com.zb.meeteat.domain.restaurant.dto.SearchRequest;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import com.zb.meeteat.domain.restaurant.entity.RestaurantReview;
import com.zb.meeteat.domain.restaurant.service.RestaurantService;
import com.zb.meeteat.exception.CustomException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
public class RestaurantController {
  private final RestaurantService restaurantService;

  @PostMapping("/search")
  public ResponseEntity<Page<Restaurant>> searchRestaurant(
      @RequestBody @Valid SearchRequest search) {

    return ResponseEntity.ok(restaurantService.getRestaurantList(search));
  }

  @GetMapping("/{restaurantId}")
  public ResponseEntity<Restaurant> getRestaurant(
      @PathVariable(name = "restaurantId") Long restaurantId) {

    Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
    if (restaurant == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(restaurant);
  }

  @GetMapping("/{restaurantId}/reviews")
  public ResponseEntity<Page<RestaurantReview>> getReviews(
      @PathVariable(name = "restaurantId") Long restaurantId,
      @RequestParam(name = "page", defaultValue = "1") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {

    return ResponseEntity.ok(
        restaurantService.getRestaurantReviews(restaurantId, page, size));
  }

  @PostMapping("/review")
  public ResponseEntity createReview (
      @ModelAttribute @Valid CreateReviewRequest req) throws CustomException {
    long userId = req.getUserId();
    RestaurantReview review = restaurantService.createReview(userId, req);
    return ResponseEntity.ok().build();
  }

  // 나의 식당 후기 조회
  @GetMapping("/myreview")
  public ResponseEntity<RestaurantReview> getMyReview(
      @RequestParam(value = "matchingHistoryId", required = true) String matchingHistoryId
  ) {

    return ResponseEntity.ok(restaurantService.getMyReviewByMatching(Long.parseLong(matchingHistoryId)));
  }
}
