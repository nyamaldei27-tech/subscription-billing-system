package com.example.demo.exception;

public class PlanNameAlreadyExistsException extends RuntimeException{
    public PlanNameAlreadyExistsException(String message) {
        super(message);
    }
}
