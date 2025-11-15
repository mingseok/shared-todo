package com.example.shared_todo.todo.repository;

import com.example.shared_todo.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByMemberId(Long memberId);

    List<Todo> findByCompleted(Boolean completed);

    List<Todo> findByMemberIdAndCompleted(Long memberId, Boolean completed);
}
