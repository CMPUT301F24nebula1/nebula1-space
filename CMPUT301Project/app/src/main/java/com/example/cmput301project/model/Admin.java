package com.example.cmput301project.model;

import android.media.Image;

import java.util.ArrayList;
import java.util.List;
import com.example.cmput301project.controller.UserController;

/**
 * Represents an Admin user.
 */
public class Admin extends User {

    public Admin(String id) {
        super(id);
        ArrayList<String> roles = getRole();
        if (!roles.contains("admin")) {
            roles.add("admin");
        }
    }



    // Additional methods specific to Admin functionality can go here
}
