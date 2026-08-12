package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public class CustomerRequest {
    @NotNull(message = "First name is Required")
    private String firstName;
    private String middleName;
    @NotNull(message = "Last name is required")
    private String lastName;
    @NotNull(message = "Customer email is required")
    private String email;

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getMiddleName() {
        return middleName;
    }
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
