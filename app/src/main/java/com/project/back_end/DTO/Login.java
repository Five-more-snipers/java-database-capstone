package com.project.back_end.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Login request payload carrying a user identifier (email or username) and password.
 */
public class Login {

    @JsonAlias({ "email", "username" })
    private String identifier;
    private String password;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEmail() {
        return identifier;
    }

    public void setEmail(String email) {
        this.identifier = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
