package com.christian.todo.repository;

import com.christian.todo.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<Todo, UUID> {

    List<Todo> findByCompleted(boolean completed);
}
