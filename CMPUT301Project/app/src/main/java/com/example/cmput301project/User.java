package com.example.cmput301project;

import java.util.ArrayList;

public class User{
    // Verifies user identity using device authentication
    private String id;
    private ArrayList<String> role = new ArrayList<String>();

    public User() {
        role.add("entrant");
    }

    public User(String id) {
        this.id = id;
        role.add("entrant");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getRole() {
        return role;
    }

    public void addOrganizerRole() {
        role.add("organizer");
    }

    public void addAdmin() {
        role.add("admin");
    }
}
