package com.example.cmput301project.model;

import java.util.ArrayList;

/**
 * Super class for entrant, organizer and admin
 * @author Xinjia Fan
 */
public class User{
    // Verifies user identity using device authentication
    private String id;
    private ArrayList<String> role = new ArrayList<String>();

    /**
     * Default constructor initializing the user with the "entrant" role.
     */
    public User() {
        role.add("entrant");
    }

    /**
     * Constructor initializing the user with a specific ID and the "entrant" role.
     * @param id The unique identifier for the user.
     */
    public User(String id) {
        this.id = id;
        role.add("entrant");
    }

    /**
     * Gets the unique identifier for the user.
     * @return The user ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the user.
     * @param id The user ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the list of roles assigned to the user.
     * @return The list of roles.
     */
    public ArrayList<String> getRole() {
        return role;
    }

    /**
     * Adds the "organizer" role to the user.
     */
    public void addOrganizerRole() {
        role.add("organizer");
    }

    /**
     * Adds the "admin" role to the user.
     */
    public void addAdmin() {
        role.add("admin");
    }
}
