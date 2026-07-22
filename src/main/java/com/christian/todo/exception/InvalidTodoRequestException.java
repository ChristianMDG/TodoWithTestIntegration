package com.christian.todo.exception;

public class InvalidTodoRequestException extends RuntimeException {

    public InvalidTodoRequestException(String message) {
        super(message);
    }
}
