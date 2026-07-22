package com.christian.todo.controller;


import com.christian.todo.dto.TodoRequest;
import com.christian.todo.dto.TodoResponse;
import com.christian.todo.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }


    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAll(
            @RequestParam(required = false) Boolean completed) {
        return ResponseEntity.ok(todoService.getAll(completed));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(todoService.getById(id));
    }

    @PutMapping
    public ResponseEntity<TodoResponse> save(@Valid @RequestBody TodoRequest request) {
        return ResponseEntity.ok(todoService.save(request));
    }
}
