package com.example.shared_todo.tag.service;

import com.example.shared_todo.tag.entity.Tag;
import com.example.shared_todo.tag.exception.TagError;
import com.example.shared_todo.tag.exception.TagException;
import com.example.shared_todo.tag.repository.TagRepository;
import com.example.shared_todo.tag.service.dto.request.CreateTagRequest;
import com.example.shared_todo.tag.service.dto.response.TagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public TagResponse createTag(CreateTagRequest request) {
        if (tagRepository.existsByName(request.getName())) {
            throw new TagException(TagError.TAG_NAME_DUPLICATED);
        }

        Tag tag = Tag.create(request.getName(), request.getColor());
        Tag savedTag = tagRepository.save(tag);
        return TagResponse.from(savedTag);
    }

    @Transactional(readOnly = true)
    public TagResponse findTagById(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new TagException(TagError.TAG_NOT_FOUND));
        return TagResponse.from(tag);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> findAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return tags.stream()
                .map(TagResponse::from)
                .toList();
    }

    @Transactional
    public void deleteTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new TagException(TagError.TAG_NOT_FOUND));
        tagRepository.delete(tag);
    }
}
