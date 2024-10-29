package com.example.cmput301project;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

public class EntrantTests {
    private Entrant entrant;
    private Event event;

    @Before
    public void setUp() {
        entrant = new Entrant();
        event = new Event();
    }

    //Test 1: Entrant successfully joins an event
    @Test
    public void testJoinEvent() {
        entrant.join_event(event);
        assertTrue(entrant.getWaitlistEventIds().contains(event.getId()));
    }

    //Test 2: Entrant successfully leaves an event
    @Test
    public void testLeaveEvent() {
        entrant.join_event(event);
        entrant.leave_event(event);
        assertFalse(entrant.getWaitlistEventIds().contains(event.getId()));
    }

    //Test 3: Entrants name is successfully set
    @Test
    public void testSetName() {
        entrant.setName("John Deere");
        assertEquals("John Deere", entrant.getName());
    }

    //Test 4: Entrants email is successfully set
    @Test
    public void testSetEmail() {
        entrant.setEmail("james.monroe@eventenjoyer.com");
        assertEquals("james.monroe@eventenjoyer.com", entrant.getEmail());
    }

    //Test 5: Entrants phone number is successfully set
    @Test
    public void testSetPhone() {
        entrant.setPhone("123-456-7890");
        assertEquals("123-456-7890", entrant.getPhone());
    }

    //Test 6: Entrants waitlist is successfully set
    @Test
    public void testSetWaitlist() {
        ArrayList<String> waitlist = new ArrayList<>();
        waitlist.add(event.getId());
    }

    //Test 7: Entrants profile picture URL is successfully set
    @Test
    public void testSetProfilePictureUrl() {
        entrant.setProfilePictureUrl("https://example.com/profile.jpg");
        assertEquals("https://example.com/profile.jpg", entrant.getProfilePictureUrl());
    }
}
