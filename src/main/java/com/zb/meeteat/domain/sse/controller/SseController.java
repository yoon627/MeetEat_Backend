package com.zb.meeteat.domain.sse.controller;

import com.zb.meeteat.domain.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {

  private final SseService sseService;

  @GetMapping("/subscribe")
  public SseEmitter subscribe() {
    return sseService.subscribe();
  }

}
