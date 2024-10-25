package com.example.cmput301project;

import java.util.ArrayList;

public class Organizer extends User{
    private ArrayList<Event> events;

    private String name;

    public Organizer() {
        this.events = new ArrayList<Event>();
    }
    public Organizer(String id) {
        super(id);
        this.events = new ArrayList<Event>();
    }

    public void create_event(Event e) {
        events.add(e);
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
