package com.example.shared_todo.common.jwt;

public interface TokenProvider {

    JwtToken generateToken(Long memberId);

    boolean validateToken(String token);

    Long extractMemberId(String token);
}
