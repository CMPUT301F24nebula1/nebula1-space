package com.example.cmput301project.model;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

/**
 * Represents an Organizer in the system, extending the User class.
 * An Organizer can create events and has personal information such as name.
 * @author Xinjia Fan
 */
public class Organizer extends User {
    @Exclude
    private transient ArrayList<Event> events;

    private String name;

    /**
     * Default constructor initializing an empty list of events and assigning the organizer role.
     */
    public Organizer() {
        this.events = new ArrayList<Event>();
        addOrganizerRole();
    }

    /**
     * Constructor initializing the organizer with a specific ID and assigning the organizer role.
     * @param id The unique identifier for the organizer.
     */
    public Organizer(String id) {
        super(id);
        addOrganizerRole();
        this.events = new ArrayList<Event>();
    }

    /**
     * Creates a new event and adds it to the list of events managed by the organizer.
     * @param e The event to create.
     */
    public void create_event(Event e) {
        events.add(e);
    }

    /**
     * Delete the event from the waitlist of the event.
     * @param e The event to delete.
     */
    public void delete_event(Event e) {
        events.remove(e);
    }

    /**
     * Gets the list of events created by the organizer.
     * @return The list of events.
     */
    public ArrayList<Event> getEvents() {
        return events;
    }

    /**
     * Gets the name of the organizer.
     * @return The name of the organizer.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the organizer.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the list of events created by the organizer.
     * @param events The list of events to set.
     */
    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }
}
