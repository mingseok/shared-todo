package com.example.shared_todo.todo.service.permission;

import com.example.shared_todo.member.entity.Member;
import com.example.shared_todo.todo.entity.Todo;
import org.springframework.stereotype.Component;

/**
 * 정책 1: 관리자는 모든 사용자의 ToDo를 수정/삭제 가능
 */
@Component
public class AdminAllAccessPermissionChecker implements TodoPermissionChecker {

    @Override
    public boolean canModify(Todo todo, Member member) {
        return member.isAdmin() || todo.isOwner(member.getId());
    }

    @Override
    public boolean canDelete(Todo todo, Member member) {
        return member.isAdmin() || todo.isOwner(member.getId());
    }
}
