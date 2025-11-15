package com.example.shared_todo.todotag.repository;

import com.example.shared_todo.todotag.entity.TodoTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoTagRepository extends JpaRepository<TodoTag, Long> {

    @Modifying
    void deleteByTodoId(Long todoId);

    List<TodoTag> findByTodoId(Long todoId);

    @Query("SELECT DISTINCT tt.todo.id FROM TodoTag tt WHERE tt.tag.id = :tagId")
    List<Long> findTodoIdsByTagId(@Param("tagId") Long tagId);
}
