package com.example.shared_todo.member.controller.open;

import com.example.shared_todo.common.dto.ApiResponse;
import com.example.shared_todo.member.service.MemberService;
import com.example.shared_todo.member.service.dto.request.SignInRequest;
import com.example.shared_todo.member.service.dto.request.SignUpRequest;
import com.example.shared_todo.member.service.dto.response.JwtTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/members")
public class MemberPublicController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(
            @Validated @RequestBody SignUpRequest request
    ) {
        memberService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.successEmpty());
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<JwtTokenResponse>> signin(
            @Validated @RequestBody SignInRequest request
    ) {
        JwtTokenResponse response = memberService.signIn(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
