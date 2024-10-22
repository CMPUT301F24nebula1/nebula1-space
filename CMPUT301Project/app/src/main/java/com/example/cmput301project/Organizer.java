package com.example.cmput301project;

import java.util.ArrayList;

public class Organizer extends User{
    private ArrayList<Event> events;
    public Organizer() {
        this.events = new ArrayList<Event>();
    }
    public void create_event() {
        Event e = new Event();
        events.add(e);
    }
}
