package com.zb.meeteat.domain.redis.service;

import com.zb.meeteat.domain.matching.dto.MatchingRequestDto;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

  private static final String MATCHING_QUEUE = "matching_queue";
  private static final String AGREE_COUNT = "agree_count";
  private static final String GROUP_SIZE = "group_size";
  private final int TEMP_TEAM_WAIT_TIME = 180;

  private final RedisTemplate<String, MatchingRequestDto> redisTemplate;
  private final RedisTemplate<String, String> redisTeamTemplate;

  public boolean isMatchingQueueEmpty() {
    return !(redisTemplate.hasKey(MATCHING_QUEUE)
        && redisTemplate.opsForList().size(MATCHING_QUEUE) > 0);
  }

  public void addMatchingQueue(MatchingRequestDto matchingRequestDto) {
    redisTemplate.opsForList().rightPush(MATCHING_QUEUE, matchingRequestDto);
  }

  public MatchingRequestDto leftPopMatchingQueue() {
    return redisTemplate.opsForList().leftPop(MATCHING_QUEUE);
  }

  public void rightPushMatchingQueue(MatchingRequestDto matchingRequestDto) {
    redisTemplate.opsForList().rightPush(MATCHING_QUEUE, matchingRequestDto);
  }

  public void makeTempTeam(String teamName, int size) {
    redisTeamTemplate.opsForHash()
        .put(teamName, AGREE_COUNT, "0");
    redisTeamTemplate.opsForHash().put(teamName, GROUP_SIZE, String.valueOf(size));
    redisTeamTemplate.expire(teamName, TEMP_TEAM_WAIT_TIME, TimeUnit.SECONDS);
  }

  public boolean isTempTeamExist(String teamName) {
    return redisTeamTemplate.hasKey(teamName);
  }

  public int getCurrentTempTeamSize(String teamName) {
    return Integer.parseInt(
        (String) Objects.requireNonNull(redisTeamTemplate.opsForHash().get(teamName, AGREE_COUNT)));
  }

  public int getTotalTempTeamSize(String teamName) {
    return Integer.parseInt(
        (String) Objects.requireNonNull(redisTeamTemplate.opsForHash().get(teamName, GROUP_SIZE)));
  }

  public void removeTempTeam(String teamName) {
    redisTeamTemplate.delete(teamName);
  }

  public void modifyCurrentTempTeamSize(String teamName) {
    redisTeamTemplate.opsForHash().increment(teamName, AGREE_COUNT, 1);
  }

}
