package com.christian.todo.service;

import com.christian.todo.dto.TodoRequest;
import com.christian.todo.dto.TodoResponse;

import java.util.List;
import java.util.UUID;

public interface TodoService {

    List<TodoResponse> getAll(Boolean completed);

    TodoResponse getById(UUID id);

    TodoResponse save(TodoRequest request);
}