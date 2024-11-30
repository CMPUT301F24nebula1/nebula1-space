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

    /**
     * Gets whether the entrant has opted to receive notifications.
     *
     * @return true if the entrant receives notifications; otherwise, false.
     */
    public  boolean getReceiveNotification() { return receiveNotification; }

    /**
     * Sets whether the entrant should receive notifications.
     *
     * @param receiveNotification true if the entrant should receive notifications; otherwise, false.
     */
    public  void setReceiveNotification(Boolean receiveNotification) {this.receiveNotification = receiveNotification; }

    /**
     * Retrieves the list of notifications for the entrant.
     *
     * @return the list of {@link Notification} objects for the entrant.
     */
    public ArrayList<Notification> getNotifications() {
        return notifications;
    }

    /**
     * Sets the list of notifications for the entrant.
     *
     * @param notifications an {@link ArrayList} of {@link Notification} objects.
     */
    public void setNotifications(ArrayList<Notification> notifications) {
        this.notifications = notifications;
    }

    /**
     * Checks if the entrant has any unread notifications.
     *
     * @param notifications an {@link ArrayList} of {@link Notification} objects to check.
     * @return true if there is at least one unread notification; otherwise, false.
     */
    static public boolean hasUnreadNotifications(ArrayList<Notification> notifications) {
        if (notifications != null) {
            for (Notification notification : notifications) {
                if (!notification.isRead()) {
                    return true; // There is at least one unread notification
                }
            }
            // All notifications are read
            return false;
        }
        else
            return false;
    }

    /**
     * Checks if this entrant is equal to another object.
     *
     * @param obj the object to compare with this entrant.
     * @return true if the given object is an {@link Entrant} with the same ID and status; otherwise, false.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Entrant)) return false;
        Entrant other = (Entrant) obj;
        return Objects.equals(getId(), other.getId()) &&
                Objects.equals(status, other.status);
    }

    /**
     * Computes the hash code for the entrant.
     *
     * @return the hash code based on the entrant's ID and status.
     */
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

    /**
     * Retrieves the latitude of the entrant's location.
     *
     * @return the latitude of the entrant's location.
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Retrieves the longitude of the entrant's location.
     *
     * @return the longitude of the entrant's location.
     */
    public Double getLongitude() {
        return longitude;
    }
}
