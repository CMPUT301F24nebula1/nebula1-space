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

    //Test 3: testing entrant getName
    @Test
    public void testGetName() {
        assertNull(entrant.getName());
    }

    //Test 4: Testing entrant setName
    @Test
    public void testSetName() {
        entrant.setName("John Deere");
        assertEquals("John Deere", entrant.getName());
    }

    //Test 5: Testing entrant getEmail
    @Test
    public void testGetEmail() {
        assertNull(entrant.getEmail());
    }

    //Test 6: Testing entrant setEmail
    @Test
    public void testSetEmail() {
        entrant.setEmail("james.monroe@eventenjoyer.com");
        assertEquals("james.monroe@eventenjoyer.com", entrant.getEmail());
    }

    //Test 7: Testing entrant getPhone
    @Test
    public void testGetPhone() {
        assertNull(entrant.getPhone());
    }

    //Test 8: Testing entrant setPhone
    @Test
    public void testSetPhone() {
        entrant.setPhone("123-456-7890");
        assertEquals("123-456-7890", entrant.getPhone());
    }

    //Test 9: Testing entrant getWaitlist
    @Test
    public void testGetWaitlist() {
        assertEquals(0, entrant.getWaitlistEventIds().size());
    }

    //Test 10: Entrants waitlist is successfully set
    @Test
    public void testSetWaitlist() {
        ArrayList<String> waitlist = new ArrayList<>();
        waitlist.add(event.getId());
        entrant.setWaitlistEventIds(waitlist);
        assertEquals(waitlist, entrant.getWaitlistEventIds());
    }

    //Test 11: Testing entrant set profile picture
    @Test
    public void testGetProfilePictureUrl() {
        assertNull(entrant.getProfilePictureUrl());
    }

    //Test 12: Entrants profile picture URL is successfully set
    @Test
    public void testSetProfilePictureUrl() {
        entrant.setProfilePictureUrl("https://example.com/profile.jpg");
        assertEquals("https://example.com/profile.jpg", entrant.getProfilePictureUrl());
    }
}
