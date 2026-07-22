package com.christian.todo.validation;


import com.christian.todo.dto.TodoRequest;
import com.christian.todo.exception.InvalidTodoRequestException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TodoRequestValidator {

    public void validate(TodoRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getId() == null) {
            errors.add("id is required");
        }
        if (request.getCreatedAt() == null) {
            errors.add("createdAt is required");
        }

        if (!errors.isEmpty()) {
            throw new InvalidTodoRequestException(String.join(", ", errors));
        }
    }
}
