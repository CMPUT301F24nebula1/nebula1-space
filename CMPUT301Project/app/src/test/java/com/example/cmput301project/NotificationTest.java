package com.example.cmput301project;

import com.example.cmput301project.model.Notification;
import com.google.firebase.Timestamp;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link Notification} class.
 */
public class NotificationTest {

    @Test
    public void testId() {
        Notification notification = new Notification();
        String testId = "12345";

        notification.setId(testId);
        assertEquals("The ID should match the set value", testId, notification.getId());
    }

    @Test
    public void testEventId() {
        Notification notification = new Notification();
        String testEventId = "event_001";

        notification.setEventId(testEventId);
        assertEquals("The event ID should match the set value", testEventId, notification.getEventId());
    }

    @Test
    public void testIsRead() {
        Notification notification = new Notification();

        notification.setRead(true);
        assertTrue("The notification should be marked as read", notification.isRead());

        notification.setRead(false);
        assertFalse("The notification should not be marked as read", notification.isRead());
    }

    @Test
    public void testMessage() {
        Notification notification = new Notification();
        String testMessage = "This is a test message.";

        notification.setMessage(testMessage);
        assertEquals("The message should match the set value", testMessage, notification.getMessage());
    }

    @Test
    public void testTimestamp() {
        Notification notification = new Notification();
        Timestamp testTimestamp = Timestamp.now();

        notification.setTimestamp(testTimestamp);
        assertEquals("The timestamp should match the set value", testTimestamp, notification.getTimestamp());
    }

    @Test
    public void testStatus() {
        Notification notification = new Notification();
        String testStatus = "New";

        notification.setStatus(testStatus);
        assertEquals("The status should match the set value", testStatus, notification.getStatus());
    }

    @Test
    public void testTitle() {
        Notification notification = new Notification();
        String testTitle = "Notification Title";

        notification.setTitle(testTitle);
        assertEquals("The title should match the set value", testTitle, notification.getTitle());
    }

    @Test
    public void testFullNotification() {
        Notification notification = new Notification();

        String testId = "12345";
        String testEventId = "event_001";
        boolean testIsRead = true;
        String testMessage = "This is a test message.";
        Timestamp testTimestamp = Timestamp.now();
        String testStatus = "New";
        String testTitle = "Notification Title";

        notification.setId(testId);
        notification.setEventId(testEventId);
        notification.setRead(testIsRead);
        notification.setMessage(testMessage);
        notification.setTimestamp(testTimestamp);
        notification.setStatus(testStatus);
        notification.setTitle(testTitle);

        assertEquals("ID should match the set value", testId, notification.getId());
        assertEquals("Event ID should match the set value", testEventId, notification.getEventId());
        assertTrue("Notification should be marked as read", notification.isRead());
        assertEquals("Message should match the set value", testMessage, notification.getMessage());
        assertEquals("Timestamp should match the set value", testTimestamp, notification.getTimestamp());
        assertEquals("Status should match the set value", testStatus, notification.getStatus());
        assertEquals("Title should match the set value", testTitle, notification.getTitle());
    }
}

