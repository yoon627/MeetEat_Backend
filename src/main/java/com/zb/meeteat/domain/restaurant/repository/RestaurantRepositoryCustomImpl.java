package com.zb.meeteat.domain.restaurant.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zb.meeteat.domain.restaurant.dto.Category;
import com.zb.meeteat.domain.restaurant.dto.RestaurantResponse;
import com.zb.meeteat.domain.restaurant.dto.SearchRequest;
import com.zb.meeteat.domain.restaurant.dto.Sort;
import com.zb.meeteat.domain.restaurant.entity.QRestaurant;
import com.zb.meeteat.domain.restaurant.entity.QRestaurantReview;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RestaurantRepositoryCustomImpl implements RestaurantRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<RestaurantResponse> findRestaurantsByFilters(
      SearchRequest searchRequest, Pageable pageable) {

    QRestaurant restaurant = QRestaurant.restaurant;
    QRestaurantReview restaurantReview = QRestaurantReview.restaurantReview;

    BooleanBuilder builder = new BooleanBuilder();
    builder.and(restaurant.roadAddressName.contains(String.valueOf(searchRequest.getRegion())));
    if (!searchRequest.getCategoryName().equals(Category.전체)) {
      builder.and(restaurant.categoryName.contains(String.valueOf(searchRequest.getCategoryName())));
    }
    builder.and(restaurant.placeName.contains(searchRequest.getPlaceName()));

    JPAQuery<Restaurant> jpaQuery = queryFactory.select(restaurant)
        .from(restaurant)
        .leftJoin(restaurantReview).on(restaurant.id.eq(restaurantReview.restaurant.id))
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize());

    // 필터링 및 정렬 처리
    if (searchRequest.getSorted() == Sort.RATING) {
      jpaQuery = jpaQuery.orderBy(restaurant.rating.desc());
    } else if (searchRequest.getSorted() == Sort.DISTANCE) {
      jpaQuery = applyDistanceSort(jpaQuery, searchRequest, restaurant);
    }

    List<Restaurant> result = jpaQuery.fetch();
    long total = Optional.ofNullable(queryFactory.select(restaurant.count())
            .from(restaurant)
            .leftJoin(restaurantReview).on(restaurant.id.eq(restaurantReview.restaurant.id))
            .where(builder)
            .fetchOne())
        .orElse(0L);

    List<RestaurantResponse> responseList = result.stream()
        .map(r -> RestaurantResponse.fromRestaurant(r, getLatestThumbnail(r)))  // 여기에서 getLatestThumbnail을 Restaurant 객체로 수정
        .toList();

    return new PageImpl<>(responseList, pageable, total);
  }

  private JPAQuery<Restaurant> applyDistanceSort(JPAQuery<Restaurant> query, SearchRequest searchRequest, QRestaurant restaurant) {
    // Haversine 공식으로 거리 계산
    NumberTemplate<Double> distanceExpression = Expressions.numberTemplate(
        Double.class,
        "6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1})))",
        searchRequest.getUserY(), restaurant.y, restaurant.x, searchRequest.getUserX()
    );
    return query.orderBy(distanceExpression.asc());
  }

  private String getLatestThumbnail(Restaurant restaurant) {
    QRestaurantReview restaurantReview = QRestaurantReview.restaurantReview;

    // 최신 리뷰의 이미지를 가져오는 쿼리
    return queryFactory.select(restaurantReview.imgUrl)
        .from(restaurantReview)
        .where(restaurantReview.restaurant.id.eq(restaurant.getId()))
        .orderBy(restaurantReview.imgUrl.desc())  // 최신 리뷰 이미지가 먼저 오도록 정렬
        .limit(1)  // 한 개만 가져오기
        .fetchFirst();  // 첫 번째 결과 반환
  }
}
