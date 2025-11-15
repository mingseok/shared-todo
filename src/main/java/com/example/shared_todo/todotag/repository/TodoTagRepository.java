package com.example.shared_todo.todotag.repository;

import com.example.shared_todo.todotag.entity.TodoTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface TodoTagRepository extends JpaRepository<TodoTag, Long> {

    @Modifying
    void deleteByTodoId(Long todoId);

    List<TodoTag> findByTodoId(Long todoId);
}
