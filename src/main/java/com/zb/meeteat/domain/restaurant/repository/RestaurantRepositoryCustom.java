package com.zb.meeteat.domain.restaurant.repository;

import com.zb.meeteat.domain.restaurant.dto.RestaurantResponse;
import com.zb.meeteat.domain.restaurant.dto.SearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantRepositoryCustom {
  Page<RestaurantResponse> findRestaurantsByFilters(SearchRequest searchRequest, Pageable pageable);
}
