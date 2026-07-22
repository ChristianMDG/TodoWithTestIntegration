package com.christian.todo.service;

import com.christian.todo.dto.TodoRequest;
import com.christian.todo.dto.TodoResponse;
import com.christian.todo.exception.TodoNotFoundException;
import com.christian.todo.model.Todo;
import com.christian.todo.repository.TodoRepository;
import com.christian.todo.validation.TodoRequestValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final TodoRequestValidator validator;

    public TodoServiceImpl(TodoRepository todoRepository, TodoRequestValidator validator) {
        this.todoRepository = todoRepository;
        this.validator = validator;
    }

    @Override
    public List<TodoResponse> getAll(Boolean completed) {
        List<Todo> todos = (completed == null)
                ? todoRepository.findAll()
                : todoRepository.findByCompleted(completed);

        return todos.stream().map(this::toResponse).toList();
    }

    @Override
    public TodoResponse getById(UUID id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
        return toResponse(todo);
    }

    @Override
    public TodoResponse save(TodoRequest request) {
        validator.validate(request);

        Todo todo = todoRepository.findById(request.getId()).orElseGet(Todo::new);
        todo.setId(request.getId());
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setCompleted(request.isCompleted());
        todo.setCreatedAt(request.getCreatedAt());
        todo.setUpdatedAt(LocalDateTime.now());

        return toResponse(todoRepository.save(todo));
    }

    private TodoResponse toResponse(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getCreatedAt(),
                todo.getUpdatedAt());
    }
}