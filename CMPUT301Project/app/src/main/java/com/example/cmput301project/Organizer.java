package com.example.cmput301project;

import java.util.ArrayList;

public class Organizer extends Entrant{
    private ArrayList<Event> events;
    public Organizer(String id) {
        super(id);
        this.events = new ArrayList<Event>();
    }
    public void create_event(Event e) {
        events.add(e);
    }
}
