package com.example.shared_todo.todo.service;

import com.example.shared_todo.todo.entity.Todo;
import com.example.shared_todo.todo.exception.TodoError;
import com.example.shared_todo.todo.exception.TodoException;
import com.example.shared_todo.todo.repository.TodoRepository;
import com.example.shared_todo.todo.service.dto.request.CreateTodoRequest;
import com.example.shared_todo.todo.service.dto.request.UpdateTodoRequest;
import com.example.shared_todo.todo.service.dto.response.TodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public TodoResponse findTodoById(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoException(TodoError.TODO_NOT_FOUND));
        return TodoResponse.from(todo);
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> findTodos(Long memberId, Boolean completed) {
        List<Todo> todos = findTodosByCondition(memberId, completed);
        return todos.stream()
                .map(TodoResponse::from)
                .toList();
    }

    private List<Todo> findTodosByCondition(Long memberId, Boolean completed) {
        if (memberId != null && completed != null) {
            return todoRepository.findByMemberIdAndCompleted(memberId, completed);
        }
        if (memberId != null) {
            return todoRepository.findByMemberId(memberId);
        }
        if (completed != null) {
            return todoRepository.findByCompleted(completed);
        }
        return todoRepository.findAll();
    }
}
