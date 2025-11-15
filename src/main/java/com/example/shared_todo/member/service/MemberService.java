package com.example.shared_todo.member.service;

import com.example.shared_todo.common.jwt.JwtToken;
import com.example.shared_todo.common.jwt.TokenProvider;
import com.example.shared_todo.member.entity.Member;
import com.example.shared_todo.member.exception.MemberError;
import com.example.shared_todo.member.exception.MemberException;
import com.example.shared_todo.member.repository.MemberRepository;
import com.example.shared_todo.member.service.dto.request.SignInRequest;
import com.example.shared_todo.member.service.dto.request.SignUpRequest;
import com.example.shared_todo.member.service.dto.response.JwtTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new MemberException(MemberError.EMAIL_DUPLICATED);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Member member = Member.create(
                request.getEmail(),
                request.getNickname(),
                encodedPassword
        );
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public JwtTokenResponse signIn(SignInRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new MemberException(MemberError.EMAIL_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new MemberException(MemberError.PASSWORD_NOT_MATCHED);
        }

        JwtToken token = tokenProvider.generateToken(member.getId());
        return JwtTokenResponse.from(token);
    }
}
