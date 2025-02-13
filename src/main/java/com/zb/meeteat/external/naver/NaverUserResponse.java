package com.zb.meeteat.external.naver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class NaverUserResponse {

    @JsonProperty("response")
    private Response response;

    @Getter
    public static class Response {
        private String id;
        private String email;
        private String nickname;
    }
}
