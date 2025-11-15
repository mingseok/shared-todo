package com.example.shared_todo.member.service.dto.response;

import com.example.shared_todo.common.jwt.JwtToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtTokenResponse {

    private final String grantType;
    private final String accessToken;

    public static JwtTokenResponse from(JwtToken token) {
        return new JwtTokenResponse(token.getGrantType(), token.getAccessToken());
    }
}
