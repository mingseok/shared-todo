package com.example.shared_todo.todo.service.permission;

import com.example.shared_todo.member.entity.Member;
import com.example.shared_todo.todo.entity.Todo;

public interface TodoPermissionChecker {

    boolean canModify(Todo todo, Member member);

    boolean canDelete(Todo todo, Member member);
}
