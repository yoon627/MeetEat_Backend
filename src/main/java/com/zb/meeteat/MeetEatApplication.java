package com.zb.meeteat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class MeetEatApplication {

  public static void main(String[] args) {
    SpringApplication.run(MeetEatApplication.class, args);
  }

}
