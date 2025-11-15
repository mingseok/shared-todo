package com.example.shared_todo.tag.entity;

import com.example.shared_todo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "color", length = 7)
    private String color;

    @Builder
    public Tag(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public static Tag create(String name, String color) {
        return Tag.builder()
                .name(name)
                .color(color)
                .build();
    }
}
