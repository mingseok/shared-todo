package com.example.shared_todo.todo.repository;

import com.example.shared_todo.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    Optional<Todo> findByIdAndDeletedAtIsNull(Long id);

    List<Todo> findByDeletedAtIsNull();

    List<Todo> findByMemberIdAndDeletedAtIsNull(Long memberId);

    List<Todo> findByCompletedAndDeletedAtIsNull(Boolean completed);

    List<Todo> findByMemberIdAndCompletedAndDeletedAtIsNull(Long memberId, Boolean completed);

    List<Todo> findByIdInAndDeletedAtIsNull(List<Long> ids);

    List<Todo> findByIdInAndCompletedAndDeletedAtIsNull(List<Long> ids, Boolean completed);

    List<Todo> findByIdInAndMemberIdAndDeletedAtIsNull(List<Long> ids, Long memberId);

    List<Todo> findByIdInAndMemberIdAndCompletedAndDeletedAtIsNull(List<Long> ids, Long memberId, Boolean completed);
}
