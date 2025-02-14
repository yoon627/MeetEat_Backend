package com.zb.meeteat;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@EnableScheduling
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class MeetEatApplication {

  public static void main(String[] args) {
    SpringApplication.run(MeetEatApplication.class, args);
  }

}
