package com.example.shared_todo.todotag.entity;

import com.example.shared_todo.tag.entity.Tag;
import com.example.shared_todo.todo.entity.Todo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "todo_tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    private TodoTag(Todo todo, Tag tag) {
        this.todo = todo;
        this.tag = tag;
    }

    public static TodoTag of(Todo todo, Tag tag) {
        return new TodoTag(todo, tag);
    }
}
