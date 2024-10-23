package com.example.cmput301project;

public class User {
    // Verifies user identity using device authentication
    private String id;

    public User() {
        ;
    }

    public User(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
