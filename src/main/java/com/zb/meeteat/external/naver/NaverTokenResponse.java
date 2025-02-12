package com.zb.meeteat.external.naver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class NaverTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
}
