package com.example.shared_todo.share.repository;

import com.example.shared_todo.share.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareRepository extends JpaRepository<Share, Long> {

    Optional<Share> findByShareToken(String shareToken);

    Optional<Share> findByTodoId(Long todoId);

    boolean existsByTodoId(Long todoId);
}
