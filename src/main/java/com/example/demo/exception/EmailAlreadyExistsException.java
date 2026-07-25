package com.example.demo.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);//call the parent constructor(RuntimeException)
    }
}
