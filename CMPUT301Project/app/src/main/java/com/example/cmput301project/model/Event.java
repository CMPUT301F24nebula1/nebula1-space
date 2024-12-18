package com.example.cmput301project.model;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Observable;
import java.util.UUID;

/**
 * Represents an Event in the system.
 * An Event has a waitlist of entrants, a limit for participants, and information such as name, description, poster URL, and QR codes.
 * @author Xinjia Fan
 */
public class Event extends Observable implements Serializable {
    private ArrayList<String> waitlistEntrantIds;
    private int limit;
    private String name;
    private String description;
    private String posterUrl;
    private String qrCode;
    private String hashedQRCode;
    private String startDate;
    private String endDate;
    private final String id;
    private boolean requiresGeolocation;
    private boolean isFinalized = false;
    private String organizerId;
    private transient Timestamp timestamp;
    private boolean isQrRemoved = false;


    /**
     * Default constructor initializing an empty waitlist and a unique ID for the event.
     */
    public Event() {
        this.waitlistEntrantIds = new ArrayList<String>();
        this.id = UUID.randomUUID().toString();
        this.limit = 0;
        this.requiresGeolocation = false;
    }

    /**
     * Constructor initializing the event with a specific name and a unique ID.
     * @param name The name of the event.
     */
    public Event(String name) {
        this.waitlistEntrantIds = new ArrayList<String>();
        this.name = name;
        this.id = UUID.randomUUID().toString();
        this.limit = 0;
        this.requiresGeolocation = false;
    }

    /**
     * Gets the waitlist.
     * @return waitlist.
     */
    public ArrayList<String> getWaitlistEntrantIds() {
        return waitlistEntrantIds;
    }

    /**
     * Sets the list of entrant IDs currently on the waitlist for this event.
     *
     * @param waitlistEntrantIds an {@link ArrayList} of entrant IDs representing the waitlisted entrants.
     */
    public void setWaitlistEntrantIds(ArrayList<String> waitlistEntrantIds) {
        this.waitlistEntrantIds = waitlistEntrantIds;
    }

    /**
     * Retrieves the ID of the organizer associated with this event.
     *
     * @return the organizer's ID as a {@link String}.
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * Sets the ID of the organizer associated with this event.
     *
     * @param organizerId the organizer's ID as a {@link String}.
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * Adds an entrant to the event's waitlist.
     * @param e The entrant to add.
     */
    public void add_entrant(Entrant e) {
        waitlistEntrantIds.add(e.getId());
    }

    /**
     * Removes an entrant from the event's waitlist.
     * @param e The entrant to remove.
     */
    public void remove_entrant(Entrant e) {
        waitlistEntrantIds.remove(e);
    }

    /**
     * Gets the limit for the number of participants in the event.
     * @return The participant limit.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Sets the limit for the number of participants in the event.
     * @param limit The participant limit to set.
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Gets the description of the event.
     * @return The description of the event.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the event.
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the name of the event.
     * @return The name of the event.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the event.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the URL of the event's poster.
     * @return The poster URL.
     */
    public String getPosterUrl() {
        return posterUrl;
    }

    /**
     * Sets the URL of the event's poster.
     * @param posterUrl The poster URL to set.
     */
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    /**
     * Gets the unique identifier for the event.
     * @return The unique event ID.
     */
    public String getId() {
        return id;
    }


    /**
     * Gets the hashed QR code of the event.
     * @return The hashed QR code.
     */
    public String getHashedQRCode() {
        return hashedQRCode;
    }

    /**
     * Sets the hashed QR code for the event.
     * @param hashedQRCode The hashed QR code to set.
     */
    public void setHashedQRCode(String hashedQRCode) {
        this.hashedQRCode = hashedQRCode;
    }

    /**
     * Gets the QR code url of the event.
     * @return The QR code url.
     */
    public String getQrCode() {
        return qrCode;
    }

    /**
     * Sets the QR code url for the event.
     * @param qrCode The QR code url to set.
     */
    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    /**
     * Gets the start date of the event.
     *
     * @return the start date as a string
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of the event.
     *
     * @param startDate the start date string to be set
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the end date of the event.
     *
     * @return the end date as a string
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of the event.
     *
     * @param endDate the end date string to be set
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * Does this event need geolocation?
     * @return true if geolocation is required for this event
     */
    @PropertyName("requiresGeolocation")
    public boolean isRequiresGeolocation() {
        return requiresGeolocation;
    }


    /**
     * set geolocate reqs
     * @param requiresGeolocation true if geolocation for this event is required
     */
    @PropertyName("requiresGeolocation")
    public void setRequiresGeolocation(boolean requiresGeolocation) {
        this.requiresGeolocation = requiresGeolocation;
    }

    /**
     * Retrieves the timestamp associated with this object.
     *
     * @return the {@link Timestamp} of the object.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp for this object.
     *
     * @param timestamp the {@link Timestamp} to be set.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Checks whether the QR code associated with this object has been removed.
     *
     * @return true if the QR code is removed; otherwise, false.
     */
    @PropertyName("isQrRemoved")
    public boolean isQrRemoved() {
        return isQrRemoved;
    }

    /**
     * Sets the status of whether the QR code associated with this object has been removed.
     *
     * @param qrRemoved true if the QR code is removed; otherwise, false.
     */
    public void setQrRemoved(boolean qrRemoved) {
        isQrRemoved = qrRemoved;
    }

    /**
     * Checks whether this object has been finalized.
     *
     * @return true if the object is finalized; otherwise, false.
     */
    @PropertyName("isFinalized")
    public boolean isFinalized() {
        return isFinalized;
    }

    /**
     * Sets the status of whether this object has been finalized.
     *
     * @param finalized true if the object is finalized; otherwise, false.
     */
    @PropertyName("isFinalized")
    public void setFinalized(boolean finalized) {
        isFinalized = finalized;
    }
}
