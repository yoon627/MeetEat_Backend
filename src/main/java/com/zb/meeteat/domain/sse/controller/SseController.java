package com.zb.meeteat.domain.sse.controller;

import com.zb.meeteat.domain.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

  private final SseService sseService;

  @GetMapping("/subscribe")
  public SseEmitter subscribe() {
    return sseService.subscribe();
  }

  @GetMapping("/connection-check")
  public boolean checkConnection() {
    return sseService.selfCheckConnection();
  }

  @PostMapping("/unsubscribe/{userId}")
  public void unsubscribe(@PathVariable Long userId) {
    sseService.unsubscribe(userId);
    sseService.addCancelledUserSet(userId);
  }
}
