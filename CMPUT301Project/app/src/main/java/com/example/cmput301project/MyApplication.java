package com.example.cmput301project;

import android.app.Application;

public class MyApplication extends Application {
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
