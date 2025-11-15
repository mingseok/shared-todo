package com.example.shared_todo.tag.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTagRequest {

    @NotBlank(message = "태그 이름을 입력해주세요.")
    @Size(max = 50, message = "태그 이름은 50자 이내로 입력해 주세요.")
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 hex 형식이어야 합니다. (예: #FF5733)")
    private String color;
}
