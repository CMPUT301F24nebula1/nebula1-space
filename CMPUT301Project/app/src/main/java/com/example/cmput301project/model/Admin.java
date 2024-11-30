package com.example.cmput301project.model;

import java.util.ArrayList;

/**
 * Represents an Admin user.
 */
public class Admin extends User {

    /**
     * Constructs an Admin user with the specified ID.
     * The "admin" role is automatically added to the user's roles if not already present.
     *
     * @param id the unique identifier for the Admin user
     */
    public Admin(String id) {
        super(id);
        ArrayList<String> roles = getRole();
        if (!roles.contains("admin")) {
            roles.add("admin");
        }
    }

}
