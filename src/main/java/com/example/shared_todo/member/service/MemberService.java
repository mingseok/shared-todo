package com.example.shared_todo.member.service;

import com.example.shared_todo.member.entity.Member;
import com.example.shared_todo.member.exception.MemberError;
import com.example.shared_todo.member.exception.MemberException;
import com.example.shared_todo.member.repository.MemberRepository;
import com.example.shared_todo.member.service.dto.request.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
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
}
