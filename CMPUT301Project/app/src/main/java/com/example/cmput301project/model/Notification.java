package com.example.cmput301project.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

/**
 * Represents a notification entity with details such as event ID, status, timestamp, and message content.
 * @author Xinjia Fan
 */
public class Notification {
    private String id;
    private String eventId;
    private boolean isRead;
    private String message;
    private Timestamp timestamp;
    private String status;
    private String title;

    /**
     * Retrieves the unique ID of the notification.
     *
     * @return the notification ID as a {@link String}.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the notification.
     *
     * @param id the notification ID as a {@link String}.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Retrieves the ID of the event associated with this notification.
     *
     * @return the event ID as a {@link String}.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the ID of the event associated with this notification.
     *
     * @param eventId the event ID as a {@link String}.
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Checks whether the notification has been read.
     *
     * @return true if the notification is read; otherwise, false.
     */
    @PropertyName("isRead")
    public boolean isRead() {
        return isRead;
    }

    /**
     * Sets the read status of the notification.
     *
     * @param read true if the notification is read; otherwise, false.
     */
    @PropertyName("isRead")
    public void setRead(boolean read) {
        isRead = read;
    }

    /**
     * Retrieves the message content of the notification.
     *
     * @return the message content as a {@link String}.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message content of the notification.
     *
     * @param message the message content as a {@link String}.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Retrieves the timestamp associated with the notification.
     *
     * @return the timestamp as a {@link Timestamp}.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp for the notification.
     *
     * @param timestamp the timestamp as a {@link Timestamp}.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Retrieves the status of the notification.
     *
     * @return the status as a {@link String}.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the notification.
     *
     * @param status the status as a {@link String}.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Retrieves the title of the notification.
     *
     * @return the title as a {@link String}.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the notification.
     *
     * @param title the title as a {@link String}.
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
