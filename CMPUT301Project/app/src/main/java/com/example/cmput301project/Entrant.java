package com.example.cmput301project;

import java.util.ArrayList;

public class Entrant extends User{
    protected ArrayList<String> waitlistEventIds;
    protected String name;
    protected String email;
    protected String phone;

    public Entrant() {
        this.waitlistEventIds = new ArrayList<String>();;
    }
    public Entrant(String id) {
        super(id);
        this.waitlistEventIds = new ArrayList<String>();
    }

    public void join_event(Event event) {
        event.add_entrant(this);
    }

    public void leave_event(Event event) {
        event.remove_entrant(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
