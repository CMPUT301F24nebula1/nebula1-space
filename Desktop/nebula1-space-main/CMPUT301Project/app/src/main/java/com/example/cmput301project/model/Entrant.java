package com.example.cmput301project.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an Entrant, extending the User class.
 * An Entrant can join or leave events and has personal information such as name, email, phone, and profile picture URL.
 * Author: Xinjia Fan
 */
public class Entrant extends User {
    protected transient ArrayList<String> waitlistEventIds;
    protected String name;
    protected String email;
    protected String phone;
    private String profilePictureUrl;
    private String status;

    /**
     * Default constructor initializing an empty waitlist.
     */
    public Entrant() {
        this.waitlistEventIds = new ArrayList<>();
        this.status = "WAITING"; // 默认状态
    }

    /**
     * Constructor initializing the entrant with a specific ID.
     * @param id The unique identifier for the entrant.
     */
    public Entrant(String id) {
        super(id);
        this.waitlistEventIds = new ArrayList<>();
        this.status = "WAITING"; // 默认状态
    }

    /**
     * Adds the entrant to an event's waitlist and adds the event ID to the entrant's waitlist.
     * @param event The event to join.
     */
    public void join_event(Event event) {
        event.add_entrant(this);
        waitlistEventIds.add(event.getId());
    }

    /**
     * Removes the entrant from an event's waitlist and removes the event ID from the entrant's waitlist.
     * @param event The event to leave.
     */
    public void leave_event(Event event) {
        event.remove_entrant(this);
        waitlistEventIds.remove(event.getId());
    }

    /**
     * Gets the name of the entrant.
     * @return The name of the entrant.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the entrant.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the email of the entrant.
     * @return The email of the entrant.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the entrant.
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the phone number of the entrant.
     * @return The phone number of the entrant.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number of the entrant.
     * @param phone The phone number to set.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the list of event IDs for which the entrant is on the waitlist.
     * @return The list of event IDs on the waitlist.
     */
    public ArrayList<String> getWaitlistEventIds() {
        return waitlistEventIds;
    }

    /**
     * Sets the list of event IDs for which the entrant is on the waitlist.
     * @param waitlistEventIds The list of event IDs to set.
     */
    public void setWaitlistEventIds(ArrayList<String> waitlistEventIds) {
        this.waitlistEventIds = waitlistEventIds;
    }

    /**
     * Gets the profile picture URL of the entrant.
     * @return The profile picture URL of the entrant.
     */
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    /**
     * Sets the profile picture URL of the entrant.
     * @param profilePictureUrl The profile picture URL to set.
     */
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    /**
     * Gets the status of the entrant.
     * @return The status of the entrant.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the entrant.
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    // 重写 equals 和 hashCode 方法（可选）
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Entrant)) return false;
        Entrant other = (Entrant) obj;
        return Objects.equals(getId(), other.getId()) &&
                Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), status);
    }
}
