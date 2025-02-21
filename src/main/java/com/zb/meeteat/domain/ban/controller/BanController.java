package com.zb.meeteat.domain.ban.controller;

import com.zb.meeteat.domain.ban.service.BanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ban")
public class BanController {

  private final BanService banService;

  @PostMapping
  public void banUser(@RequestParam long bannedId) {
    banService.banUser(bannedId);
  }

  @DeleteMapping
  public void deleteBan(@RequestParam long bannedId) {
    banService.deleteBan(bannedId);
  }

  @GetMapping
  public ResponseEntity<Boolean> checkBan(@RequestParam long bannedId) {
    return ResponseEntity.ok(banService.checkBan(bannedId));
  }
}
