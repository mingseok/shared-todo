package com.example.shared_todo.todo.service.permission;

import com.example.shared_todo.member.entity.Member;
import com.example.shared_todo.todo.entity.Todo;
import org.springframework.stereotype.Component;

/**
 * 정책 2: 관리자도 본인이 생성한 ToDo만 수정/삭제 가능
 */
@Component
public class OwnerOnlyPermissionChecker implements TodoPermissionChecker {

    @Override
    public boolean canModify(Todo todo, Member member) {
        return todo.isOwner(member.getId());
    }

    @Override
    public boolean canDelete(Todo todo, Member member) {
        return todo.isOwner(member.getId());
    }
}
