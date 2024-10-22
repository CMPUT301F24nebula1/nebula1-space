package com.example.cmput301project;

import java.util.ArrayList;

public class Entrant extends User{
    private ArrayList<Event> events;
    private String name;
    private String email;
    private String phone;
    public Entrant(String id) {
        super(id);
        this.events = new ArrayList<Event>();
    }

    public void join_event(Event event) {
        event.add_entrant(this);
    }

    public void leave_event(Event event) {
        event.remove_entrant(this);
    }
}
