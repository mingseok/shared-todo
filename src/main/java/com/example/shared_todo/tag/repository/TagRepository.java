package com.example.shared_todo.tag.repository;

import com.example.shared_todo.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    boolean existsByName(String name);

    List<Tag> findByIdIn(List<Long> ids);
}
