package com.zb.meeteat.domain.restaurant.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "restaurant")
public class Restaurant {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long kakaomapsId;
  private String placeName;
  private String phone;
  private Double y;//lat
  private Double x;//lon
  private String roadAddressName;
  private String categoryName;
  private Double rating;
}