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

    private String email;

    private String phone;

    private String profilePictureUrl;

    private String imageUrl;

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
     * Gets the image URL associated with the Organizer.
     *
     * @return The image URL as a String.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image URL associated with the Organizer.
     *
     * @param imageUrl The image URL to set.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    /**
     * Gets the email of the user.
     *
     * @return the email as a string
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the user.
     *
     * @param email the email string to be set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the phone number of the user.
     *
     * @return the phone number as a string
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number of the user.
     *
     * @param phone the phone number string to be set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the profile picture URL of the user.
     *
     * @return the profile picture URL as a string
     */
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    /**
     * Sets the profile picture URL of the user.
     *
     * @param profilePictureUrl the profile picture URL string to be set
     */
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
