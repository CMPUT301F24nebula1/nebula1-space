package com.example.cmput301project.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;
import java.util.UUID;

/**
 * Represents an Event in the system.
 * An Event has a waitlist of entrants, a limit for participants, and information such as name, description, poster URL, and QR codes.
 * @author Xinjia Fan
 */
public class Event extends Observable implements Serializable {
    private ArrayList<Entrant> waitlist;
    private int limit;
    private String name;
    private String description;
    private String posterUrl;
    private String qrCode;
    private String hashedQRCode;
    private final String id;

    /**
     * Default constructor initializing an empty waitlist and a unique ID for the event.
     */
    public Event() {
        this.waitlist = new ArrayList<Entrant>();
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Constructor initializing the event with a specific name and a unique ID.
     * @param name The name of the event.
     */
    public Event(String name) {
        this.waitlist = new ArrayList<Entrant>();
        this.name = name;
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Gets the waitlist.
     * @return waitlist.
     */
    public ArrayList<Entrant> getWaitlist() {
        return waitlist;
    }

    /**
     * Adds an entrant to the event's waitlist.
     * @param e The entrant to add.
     */
    public void add_entrant(Entrant e) {
        waitlist.add(e);
    }

    /**
     * Removes an entrant from the event's waitlist.
     * @param e The entrant to remove.
     */
    public void remove_entrant(Entrant e) {
        waitlist.remove(e);
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
}
