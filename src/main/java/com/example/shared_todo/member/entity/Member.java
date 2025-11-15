package com.example.shared_todo.member.entity;

import com.example.shared_todo.common.BaseEntity;
import com.example.shared_todo.member.entity.type.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Builder
    public Member(String email, String password, String nickname, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : Role.USER;
    }

    public static Member create(String email, String nickname, String encodedPassword) {
        return Member.builder()
                .email(email)
                .nickname(nickname)
                .password(encodedPassword)
                .role(Role.USER)
                .build();
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
}
