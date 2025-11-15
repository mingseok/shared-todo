package com.example.shared_todo.common.interceptor;

import com.example.shared_todo.common.identity.exception.AuthError;
import com.example.shared_todo.common.identity.exception.CommentException;
import com.example.shared_todo.common.identity.resolver.AuthMemberIdArgumentResolver;
import com.example.shared_todo.common.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            throw new CommentException(AuthError.UNAUTHORIZED);
        }

        if (!tokenProvider.validateToken(token)) {
            throw new CommentException(AuthError.INVALID_TOKEN);
        }

        Long memberId = tokenProvider.extractMemberId(token);
        request.setAttribute(AuthMemberIdArgumentResolver.MEMBER_ID_ATTRIBUTE, memberId);

        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
