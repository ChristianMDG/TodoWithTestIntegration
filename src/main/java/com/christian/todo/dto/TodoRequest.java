package com.christian.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TodoRequest {

    private UUID id;

    @NotBlank(message = "title must not be blank")
    private String title;

    private String description;

    private boolean completed;

    private LocalDateTime createdAt;

    }

