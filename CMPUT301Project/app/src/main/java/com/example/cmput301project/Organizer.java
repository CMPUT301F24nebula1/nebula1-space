package com.example.cmput301project;

import java.util.ArrayList;

public class Organizer extends User{
    private transient ArrayList<Event> events;

    private String name;

    public Organizer() {
        this.events = new ArrayList<Event>();
        addOrganizerRole();
    }
    public Organizer(String id) {
        super(id);
        addOrganizerRole();
        this.events = new ArrayList<Event>();
    }

    public void create_event(Event e) {
        events.add(e);
    }

    public void delete_event(Event e) {
        events.remove(e);
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

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }
}
