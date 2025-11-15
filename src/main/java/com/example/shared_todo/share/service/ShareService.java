package com.example.shared_todo.share.service;

import com.example.shared_todo.member.entity.Member;
import com.example.shared_todo.member.exception.MemberError;
import com.example.shared_todo.member.exception.MemberException;
import com.example.shared_todo.member.repository.MemberRepository;
import com.example.shared_todo.share.entity.Share;
import com.example.shared_todo.share.exception.ShareError;
import com.example.shared_todo.share.exception.ShareException;
import com.example.shared_todo.share.repository.ShareRepository;
import com.example.shared_todo.share.service.dto.response.ShareResponse;
import com.example.shared_todo.todo.entity.Todo;
import com.example.shared_todo.todo.exception.TodoError;
import com.example.shared_todo.todo.exception.TodoException;
import com.example.shared_todo.todo.repository.TodoRepository;
import com.example.shared_todo.todo.service.dto.response.TodoResponse;
import com.example.shared_todo.todo.service.permission.AdminAllAccessPermissionChecker;
import com.example.shared_todo.todo.service.permission.OwnerOnlyPermissionChecker;
import com.example.shared_todo.todo.service.permission.TodoPermissionChecker;
import com.example.shared_todo.todotag.entity.TodoTag;
import com.example.shared_todo.todotag.repository.TodoTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareRepository shareRepository;
    private final TodoRepository todoRepository;
    private final TodoTagRepository todoTagRepository;
    private final MemberRepository memberRepository;
    private final OwnerOnlyPermissionChecker ownerOnlyPermissionChecker;
    private final AdminAllAccessPermissionChecker adminAllAccessPermissionChecker;

    @Value("${todo.permission.policy:owner-only}")
    private String permissionPolicy;

    @Transactional
    public ShareResponse createShare(Long todoId, Long memberId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TodoError.TODO_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberError.MEMBER_NOT_FOUND));

        TodoPermissionChecker permissionChecker = getPermissionChecker();
        if (!permissionChecker.canModify(todo, member)) {
            throw new TodoException(TodoError.FORBIDDEN_TODO_ACCESS);
        }

        if (shareRepository.existsByTodoId(todoId)) {
            throw new ShareException(ShareError.ALREADY_SHARED);
        }

        Share share = Share.create(todo, memberId);
        Share savedShare = shareRepository.save(share);

        return ShareResponse.from(savedShare);
    }

    @Transactional(readOnly = true)
    public TodoResponse findSharedTodo(String shareToken) {
        Share share = shareRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new ShareException(ShareError.SHARE_NOT_FOUND));

        if (share.isExpired()) {
            throw new ShareException(ShareError.SHARE_EXPIRED);
        }

        Todo todo = share.getTodo();
        List<TodoTag> todoTags = todoTagRepository.findByTodoId(todo.getId());
        return TodoResponse.from(todo, todoTags);
    }

    @Transactional
    public void deleteShare(Long todoId, Long memberId) {
        Share share = shareRepository.findByTodoId(todoId)
                .orElseThrow(() -> new ShareException(ShareError.SHARE_NOT_FOUND));

        if (!share.getSharedBy().equals(memberId)) {
            throw new ShareException(ShareError.FORBIDDEN_SHARE_DELETE);
        }

        shareRepository.delete(share);
    }

    private TodoPermissionChecker getPermissionChecker() {
        if ("admin-all-access".equals(permissionPolicy)) {
            return adminAllAccessPermissionChecker;
        }
        return ownerOnlyPermissionChecker;
    }
}
