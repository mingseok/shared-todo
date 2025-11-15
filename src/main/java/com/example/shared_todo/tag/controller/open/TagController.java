package com.example.shared_todo.tag.controller.open;

import com.example.shared_todo.common.dto.ApiResponse;
import com.example.shared_todo.tag.service.TagService;
import com.example.shared_todo.tag.service.dto.request.CreateTagRequest;
import com.example.shared_todo.tag.service.dto.response.TagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> create(
            @Validated @RequestBody CreateTagRequest request
    ) {
        TagResponse response = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> findAll() {
        List<TagResponse> responses = tagService.findAllTags();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> findById(
            @PathVariable Long id
    ) {
        TagResponse response = tagService.findTagById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(ApiResponse.successEmpty());
    }
}
