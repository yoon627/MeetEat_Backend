package com.zb.meeteat.domain.restaurant.dto;


import com.zb.meeteat.domain.restaurant.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {

  private Long id;
  private Long kakaomaps_id;
  private String place_name;
  private String phone;
  private double x;
  private double y;
  private String road_address_name;
  private String category_name;
  private double rating;
  private String thumbnail;

  public static RestaurantResponse fromRestaurant(Restaurant restaurant, String thumbnail) {
    RestaurantResponse res = new RestaurantResponse();
    res.setId(restaurant.getId());
    res.setKakaomaps_id(restaurant.getKakaomapsId());
    res.setPlace_name(restaurant.getPlaceName());
    res.setPhone(restaurant.getPhone());
    res.setX(restaurant.getX());
    res.setY(restaurant.getY());
    res.setRoad_address_name(restaurant.getRoadAddressName());
    res.setCategory_name(restaurant.getCategoryName());
    res.setRating(restaurant.getRating());
    res.setThumbnail(thumbnail);
    return res;
  }
}
