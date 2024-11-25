package com.example.cmput301project.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

//added trying to get maps to stop crashing
import java.io.Serializable;

/**
 * Represents an Entrant, extending the User class.
 * An Entrant can join or leave events and has personal information such as name, email, phone, and profile picture URL.
 * @author Xinjia Fan
 */

public class Entrant extends User implements Serializable {
    protected transient ArrayList<String> waitlistEventIds;
    protected  transient ArrayList<Notification> notifications;
    protected String name;
    protected String email;
    protected String phone;
    private String profilePictureUrl;
    private String status;
    private String initials;

    private Boolean receiveNotification = true;

    private Double latitude;
    private Double longitude;


    /**
     * Default constructor initializing an empty waitlist.
     */
    public Entrant() {
        this.waitlistEventIds = new ArrayList<String>();;
    }

    /**
     * Constructor initializing the entrant with a specific ID.
     * @param id The unique identifier for the entrant.
     */
    public Entrant(String id) {
        super(id);
        this.waitlistEventIds = new ArrayList<String>();
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
     * Gets the name of the entrant.
     * @return The initials of the entrant.
     */
    public String getInitials() {
        if (name == null || name.isEmpty()) {
            return "";
        }
        // Split the name by spaces
        String[] nameParts = name.split(" ");
        StringBuilder initials = new StringBuilder();
        // Loop through the name parts and get the first letter of each
        for (String part : nameParts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0)); // Append the first letter
            }
        }
        return initials.toString().toUpperCase(); // Return the initials in uppercase
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

    public  boolean getReceiveNotification() { return receiveNotification; }

    public  void setReceiveNotification(Boolean receiveNotification) {this.receiveNotification = receiveNotification; }

    public ArrayList<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(ArrayList<Notification> notifications) {
        this.notifications = notifications;
    }

    static public boolean hasUnreadNotifications(ArrayList<Notification> notifications) {
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                return true; // There is at least one unread notification
            }
        }
        return false; // All notifications are read
    }


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

    /**
     * setter for a users lat and long for geolocation
     * @param latitude
     * @param longitude
     */
    public void setLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
