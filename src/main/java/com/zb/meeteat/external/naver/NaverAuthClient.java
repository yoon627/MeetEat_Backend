package com.zb.meeteat.external.naver;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class NaverAuthClient {

    @Value("${naver.auth.token-url}")
    private String tokenUrl;

    @Value("${naver.auth.user-info-url}")
    private String userInfoUrl;

    @Value("${naver.auth.client-id}")
    private String clientId;

    @Value("${naver.auth.client-secret}")
    private String clientSecret;

    @Value("${naver.auth.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public NaverTokenResponse getToken(String authCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", authCode);
        params.add("state", "random-state");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<NaverTokenResponse> response = restTemplate.exchange(
                tokenUrl, HttpMethod.POST, request, NaverTokenResponse.class
        );

        return response.getBody();
    }

    public NaverUserResponse getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<NaverUserResponse> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                request,
                NaverUserResponse.class
        );

        return response.getBody();
    }
}
