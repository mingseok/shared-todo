package com.example.shared_todo.todo.service;

import com.example.shared_todo.member.entity.Member;
import com.example.shared_todo.member.exception.MemberError;
import com.example.shared_todo.member.exception.MemberException;
import com.example.shared_todo.member.repository.MemberRepository;
import com.example.shared_todo.tag.entity.Tag;
import com.example.shared_todo.tag.repository.TagRepository;
import com.example.shared_todo.todo.entity.Todo;
import com.example.shared_todo.todo.exception.TodoError;
import com.example.shared_todo.todo.exception.TodoException;
import com.example.shared_todo.todo.repository.TodoRepository;
import com.example.shared_todo.todo.service.dto.request.CreateTodoRequest;
import com.example.shared_todo.todo.service.dto.request.ReorderTodoRequest;
import com.example.shared_todo.todo.service.dto.request.TodoOrderItem;
import com.example.shared_todo.todo.service.dto.request.UpdateTodoRequest;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final TagRepository tagRepository;
    private final TodoTagRepository todoTagRepository;
    private final MemberRepository memberRepository;
    private final OwnerOnlyPermissionChecker ownerOnlyPermissionChecker;
    private final AdminAllAccessPermissionChecker adminAllAccessPermissionChecker;

    @Value("${todo.permission.policy:owner-only}")
    private String permissionPolicy;

    @Transactional
    public TodoResponse createTodo(CreateTodoRequest request, Long memberId) {
        Todo todo = Todo.create(
                request.getTitle(), request.getContent(),
                request.getDueDate(), memberId
        );
        Todo savedTodo = todoRepository.save(todo);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            addTagsToTodo(savedTodo, request.getTagIds());
        }

        return buildTodoResponse(savedTodo);
    }

    @Transactional(readOnly = true)
    public TodoResponse findTodoById(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TodoError.TODO_NOT_FOUND));
        return buildTodoResponse(todo);
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> findTodos(Long memberId, Boolean completed, Long tagId) {
        List<Todo> todos = findTodosByCondition(memberId, completed, tagId);
        List<Todo> sortedTodos = sortTodosByDisplayOrder(todos);
        return sortedTodos.stream()
                .map(this::buildTodoResponse)
                .toList();
    }

    private List<Todo> findTodosByCondition(Long memberId, Boolean completed, Long tagId) {
        if (tagId != null) {
            return findTodosByTag(memberId, completed, tagId);
        }

        if (memberId != null && completed != null) {
            return todoRepository.findByMemberIdAndCompleted(memberId, completed);
        }
        if (memberId != null) {
            return todoRepository.findByMemberId(memberId);
        }
        if (completed != null) {
            return todoRepository.findByCompleted(completed);
        }
        return todoRepository.findAll();
    }

    private List<Todo> findTodosByTag(Long memberId, Boolean completed, Long tagId) {
        List<Long> todoIds = todoTagRepository.findTodoIdsByTagId(tagId);
        
        if (todoIds.isEmpty()) {
            return Collections.emptyList();
        }

        if (memberId != null && completed != null) {
            return todoRepository.findByIdInAndMemberIdAndCompleted(todoIds, memberId, completed);
        }
        if (memberId != null) {
            return todoRepository.findByIdInAndMemberId(todoIds, memberId);
        }
        if (completed != null) {
            return todoRepository.findByIdInAndCompleted(todoIds, completed);
        }
        return todoRepository.findByIdIn(todoIds);
    }

    private List<Todo> sortTodosByDisplayOrder(List<Todo> todos) {
        return todos.stream()
                .sorted(Comparator.comparing(Todo::getDisplayOrder)
                        .thenComparing(Todo::getId))
                .toList();
    }

    @Transactional
    public TodoResponse updateTodo(Long todoId, UpdateTodoRequest request, Long memberId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TodoError.TODO_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberError.MEMBER_NOT_FOUND));

        TodoPermissionChecker permissionChecker = getPermissionChecker();
        if (!permissionChecker.canModify(todo, member)) {
            throw new TodoException(TodoError.FORBIDDEN_TODO_ACCESS);
        }

        todo.update(request.getTitle(), request.getContent(),
                request.getDueDate(), request.getCompleted());

        if (request.getTagIds() != null) {
            updateTodoTags(todoId, request.getTagIds());
        }

        return buildTodoResponse(todo);
    }

    @Transactional
    public void reorderTodos(ReorderTodoRequest request, Long memberId) {
        List<Long> todoIds = request.getOrders().stream()
                .map(TodoOrderItem::getTodoId)
                .toList();

        List<Todo> todos = todoRepository.findByIdIn(todoIds);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberError.MEMBER_NOT_FOUND));

        TodoPermissionChecker permissionChecker = getPermissionChecker();

        for (TodoOrderItem orderItem : request.getOrders()) {
            Todo todo = todos.stream()
                    .filter(t -> t.getId().equals(orderItem.getTodoId()))
                    .findFirst()
                    .orElseThrow(() -> new TodoException(TodoError.TODO_NOT_FOUND));

            if (!permissionChecker.canModify(todo, member)) {
                throw new TodoException(TodoError.FORBIDDEN_TODO_ACCESS);
            }

            todo.updateDisplayOrder(orderItem.getDisplayOrder());
        }
    }

    @Transactional
    public void deleteTodo(Long todoId, Long memberId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TodoError.TODO_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberError.MEMBER_NOT_FOUND));

        TodoPermissionChecker permissionChecker = getPermissionChecker();
        if (!permissionChecker.canDelete(todo, member)) {
            throw new TodoException(TodoError.FORBIDDEN_TODO_ACCESS);
        }

        todoTagRepository.deleteByTodoId(todoId);
        todoRepository.delete(todo);
    }

    private TodoPermissionChecker getPermissionChecker() {
        if ("admin-all-access".equals(permissionPolicy)) {
            return adminAllAccessPermissionChecker;
        }
        return ownerOnlyPermissionChecker;
    }

    private void addTagsToTodo(Todo todo, List<Long> tagIds) {
        List<Tag> tags = tagRepository.findByIdIn(tagIds);
        for (Tag tag : tags) {
            TodoTag todoTag = TodoTag.of(todo, tag);
            todoTagRepository.save(todoTag);
        }
    }

    private void updateTodoTags(Long todoId, List<Long> tagIds) {
        todoTagRepository.deleteByTodoId(todoId);
        if (!tagIds.isEmpty()) {
            Todo todo = todoRepository.findById(todoId)
                    .orElseThrow(() -> new TodoException(TodoError.TODO_NOT_FOUND));
            addTagsToTodo(todo, tagIds);
        }
    }

    private TodoResponse buildTodoResponse(Todo todo) {
        List<TodoTag> todoTags = todoTagRepository.findByTodoId(todo.getId());
        return TodoResponse.from(todo, todoTags);
    }
}
