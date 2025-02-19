package com.zb.meeteat.domain.restaurant.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zb.meeteat.domain.restaurant.dto.RestaurantResponse;
import com.zb.meeteat.domain.restaurant.dto.RestaurantReviewsResponse;
import com.zb.meeteat.domain.restaurant.dto.SearchRequest;
import com.zb.meeteat.domain.restaurant.dto.Sort;
import com.zb.meeteat.domain.restaurant.entity.QRestaurant;
import com.zb.meeteat.domain.restaurant.entity.QRestaurantReview;
import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import com.zb.meeteat.domain.restaurant.entity.RestaurantReview;
import com.zb.meeteat.domain.user.entity.QUser;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RestaurantReviewRepositoryCustomImpl implements RestaurantReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<RestaurantReviewsResponse> findRestaurantReviewsByRestaurantId(Long restaurantId, Pageable pageable) {
    QRestaurantReview restaurantReview = QRestaurantReview.restaurantReview;
    QUser user = QUser.user;

    BooleanBuilder builder = new BooleanBuilder();
    builder.and(restaurantReview.restaurant.id.eq(restaurantId));

    // 기본 쿼리
    JPAQuery<RestaurantReview> query = queryFactory.selectFrom(restaurantReview)
        .join(restaurantReview.user, user)  // 사용자와의 조인
        .where(builder)
        .orderBy(restaurantReview.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize());

    // 결과 페칭
    List<RestaurantReview> result = query.fetch();

    // 전체 리뷰의 수를 구하기 위한 쿼리
    long total = Optional.ofNullable(queryFactory.select(restaurantReview.count())
        .from(restaurantReview)
        .where(builder)
        .fetchOne())
        .orElse(0L);

    // RestaurantReviewResponse 변환
    List<RestaurantReviewsResponse> responseList = result.stream()
        .map(r -> new RestaurantReviewsResponse(
            r.getId(),
            r.getRating(),
            r.getDescription(),
            r.getImgUrl(),
            r.getUser().getNickname(),
            r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ))
        .collect(Collectors.toList());

    return new PageImpl<>(responseList, pageable, total);
  }
}