package com.example.cmput301project;

import java.util.ArrayList;

public class Organizer extends Entrant{
    private ArrayList<Event> events;

    public Organizer() {
        ;
    }
    public Organizer(String id) {
        super(id);
        this.events = new ArrayList<Event>();
    }

    public Organizer(Entrant e) {
        super(e.getId());
        this.name = e.getName();
        this.joinedEvents = e.joinedEvents;
        this.phone = e.phone;
    }
    public void create_event(Event e) {
        events.add(e);
    }

    public ArrayList<Event> getEvents() {
        return events;
    }
}
