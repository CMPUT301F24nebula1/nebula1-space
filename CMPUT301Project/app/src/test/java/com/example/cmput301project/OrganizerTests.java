package com.example.cmput301project;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

public class OrganizerTests {
    private Organizer organizer;
    private Event event;

    @Before
    public void setUp() {
        organizer = new Organizer();
        event = new Event();
    }

    //Test 1: Organizer successfully creates an event
    @Test
    public void testCreateEvent() {
        organizer.create_event(event);
        assertTrue(organizer.getEvents().contains(event));
    }

    //Test 2: Organizer successfully deletes an event
    @Test
    public void testDeleteEvent() {
        organizer.create_event(event);
        organizer.delete_event(event);
        assertFalse(organizer.getEvents().contains(event));
    }

    //Test 3: Testing organizer getEvents
    @Test
    public void testGetEvents() {
        assertEquals(0, organizer.getEvents().size());
    }

    //Test 4: Testing organizer setEvents
    @Test
    public void testSetEvents() {
        ArrayList<Event> events = new ArrayList<>();
        events.add(event);
        organizer.setEvents(events);
        assertEquals(events, organizer.getEvents());
    }

    //Test 5: Testing organizer getName
    @Test
    public void testGetName() {
        assertNull(organizer.getName());
    }

    //Test 6: Testing organizer setName
    @Test
    public void testSetName() {
        organizer.setName("John Deere");
        assertEquals("John Deere", organizer.getName());
    }
}
