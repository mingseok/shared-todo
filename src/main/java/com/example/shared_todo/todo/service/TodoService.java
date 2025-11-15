package com.example.shared_todo.todo.service;

import com.example.shared_todo.todo.entity.Todo;
import com.example.shared_todo.todo.repository.TodoRepository;
import com.example.shared_todo.todo.service.dto.request.CreateTodoRequest;
import com.example.shared_todo.todo.service.dto.response.TodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    @Transactional
    public TodoResponse createTodo(CreateTodoRequest request, Long memberId) {
        Todo todo = Todo.create(
                request.getTitle(), request.getContent(),
                request.getDueDate(), memberId
        );
        Todo savedTodo = todoRepository.save(todo);
        return TodoResponse.from(savedTodo);
    }
}
