package com.zb.meeteat.domain.restaurant.repository;

import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>,
    RestaurantRepositoryCustom {

  Restaurant findByPlaceNameAndRoadAddressName(String name, String roadAddressName);
}