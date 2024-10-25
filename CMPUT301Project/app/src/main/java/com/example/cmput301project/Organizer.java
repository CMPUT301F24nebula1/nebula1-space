package com.example.cmput301project;

import java.util.ArrayList;

public class Organizer extends User{
    private ArrayList<Event> events;

    private ArrayList<Event> joinedEvents;
    private String name;
    private String email;
    private String phone;

    public Organizer() {
        this.joinedEvents = new ArrayList<Event>();
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
