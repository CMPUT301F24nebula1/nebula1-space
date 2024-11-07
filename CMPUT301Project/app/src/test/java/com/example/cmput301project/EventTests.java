package com.example.cmput301project;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;

import java.util.UUID;

public class EventTests {
    private Event event;

    @Before
    public void setUp() {
        event = new Event();
    }

    //Test 1: Testing add_entrant
    @Test
    public void testAddEntrant() {
        Entrant entrant = new Entrant();
        event.add_entrant(entrant);
        assertTrue(event.getWaitlistEntrantIds().contains(entrant));
    }

    //Test 2: Testing remove entrant
    @Test
    public void testRemoveEntrant() {
        Entrant entrant = new Entrant();
        event.add_entrant(entrant);
        event.remove_entrant(entrant);
        assertFalse(event.getWaitlistEntrantIds().contains(entrant));
    }

    //Test 3: Testing get limit
    @Test
    public void testGetLimit() {
        assertEquals(0, event.getLimit());
    }

    //Test 4: Testing set limit
    @Test
    public void testSetLimit() {
        event.setLimit(100);
        assertEquals(100, event.getLimit());
    }


    //Test 5: Testing get description
    @Test
    public void testGetDescription() {
        assertNull(event.getDescription());
    }

    //Test 6: Testing set description
    @Test
    public void testSetDescription() {
        event.setDescription("This is a test event.");
        assertEquals("This is a test event.", event.getDescription());
    }

    //Test 7: Testing get name
    @Test
    public void testGetName() {
        assertNull(event.getName());
    }

    //Test 8: Testing set name
    @Test
    public void testSetName() {
        event.setName("Test Event");
        assertEquals("Test Event", event.getName());
    }


    //Test 9: Getting poster URL
    @Test
    public void testGetPosterUrl() {
        assertNull(event.getPosterUrl());
    }

    //Test 10: Testing set poster URL
    @Test
    public void testSetPosterUrl() {
        event.setPosterUrl("https://example.com/poster.jpg");
        assertEquals("https://example.com/poster.jpg", event.getPosterUrl());
    }

    //Test 11: Testing get ID
    @Test
    public void testGetId() {
        String id = event.getId();
        assertNotNull(id);
        assertTrue(UUID.fromString(id).version() == 4);
    }

    //Test 12: Get hashed QR code
    @Test
    public void testGetHashedQRCode() {
        assertNull(event.getHashedQRCode());
    }

    //Test 13: Set hashed QR code
    @Test
    public void testSetHashedQRCode() {
        event.setHashedQRCode("hashed_qr_code");
        assertEquals("hashed_qr_code", event.getHashedQRCode());
    }

    //Test 14: Get QR code
    @Test
    public void testGetQrCode() {
        assertNull(event.getQrCode());
    }

    //Test 15: Set QR code
    @Test
    public void testSetQrCode() {
        event.setQrCode("qr_code");
        assertEquals("qr_code", event.getQrCode());

    }
}
