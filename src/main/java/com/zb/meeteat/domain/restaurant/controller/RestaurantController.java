package com.zb.meeteat.domain.restaurant.controller;

import com.zb.meeteat.domain.restaurant.dto.CreateReviewRequest;
import com.zb.meeteat.domain.restaurant.dto.RestaurantResponse;
import com.zb.meeteat.domain.restaurant.dto.SearchRequest;
import com.zb.meeteat.domain.restaurant.entity.RestaurantReview;
import com.zb.meeteat.domain.restaurant.service.RestaurantService;
import com.zb.meeteat.exception.UserCustomException;
import com.zb.meeteat.jwt.JwtUtil;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
public class RestaurantController {
  private final RestaurantService restaurantService;
  private final JwtUtil jwtUtil;

  @PostMapping("/search")
  public ResponseEntity<Page<RestaurantResponse>> searchRestaurant(
      @RequestBody @Valid SearchRequest search) {

    return ResponseEntity.ok(restaurantService.getRestaurantList(search));
  }

  @GetMapping("/{restaurantId}")
  public ResponseEntity<RestaurantResponse> getRestaurant(
      @PathVariable(name = "restaurantId") Long restaurantId) {

    RestaurantResponse restaurant = restaurantService.getRestaurant(restaurantId);
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
      @RequestHeader("Authorization") String token,
      @ModelAttribute @Valid CreateReviewRequest req) throws UserCustomException {
    // 토큰에서 userId 추출
    long userId = jwtUtil.getUserId(token.replace("Bearer ", "")); // "Bearer "를 제거하고 토큰을 전달

    RestaurantReview review = restaurantService.createReview(userId, req);
    return ResponseEntity.ok().build();
  }

  // 나의 식당 후기 조회
  @GetMapping("/myreview")
  public ResponseEntity<RestaurantReview> getMyReview(
      @RequestHeader("Authorization") String token,
      @RequestParam(value = "matchingHistoryId", required = true) String matchingHistoryId
  ) {

    jwtUtil.validateToken(token);
    return ResponseEntity.ok(restaurantService.getMyReviewByMatching(Long.parseLong(matchingHistoryId)));
  }
}
