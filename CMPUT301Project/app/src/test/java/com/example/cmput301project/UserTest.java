package com.example.cmput301project;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

import com.example.cmput301project.model.User;

/**
 * Unit tests for the {@link User} class.
 */
public class UserTest {

    @Test
    public void testParameterizedConstructor() {
        String testId = "user123";
        User user = new User(testId);

        assertEquals("User ID should match the provided value", testId, user.getId());
        ArrayList<String> roles = user.getRole();
        assertNotNull("Role list should not be null", roles);
        assertEquals("Role list should contain exactly 1 role", 1, roles.size());
        assertTrue("Default role should be 'entrant'", roles.contains("entrant"));
    }

    @Test
    public void testSetId() {
        User user = new User();
        String testId = "user123";

        user.setId(testId);
        assertEquals("User ID should match the set value", testId, user.getId());
    }

    @Test
    public void testGetRole() {
        User user = new User();

        ArrayList<String> roles = user.getRole();
        assertNotNull("Role list should not be null", roles);
        assertEquals("Role list should contain exactly 1 role", 1, roles.size());
        assertTrue("Default role should be 'entrant'", roles.contains("entrant"));
    }

    @Test
    public void testAddOrganizerRole() {
        User user = new User();

        user.addOrganizerRole();
        ArrayList<String> roles = user.getRole();
        assertEquals("Role list should contain 2 roles after adding 'organizer'", 2, roles.size());
        assertTrue("Role list should contain 'organizer'", roles.contains("organizer"));
    }

    @Test
    public void testAddAdminRole() {
        User user = new User();

        user.addAdmin();
        ArrayList<String> roles = user.getRole();
        assertEquals("Role list should contain 2 roles after adding 'admin'", 2, roles.size());
        assertTrue("Role list should contain 'admin'", roles.contains("admin"));
    }

    @Test
    public void testAddMultipleRoles() {
        User user = new User();

        user.addOrganizerRole();
        user.addAdmin();

        ArrayList<String> roles = user.getRole();
        assertEquals("Role list should contain 3 roles after adding 'organizer' and 'admin'", 3, roles.size());
        assertTrue("Role list should contain 'entrant'", roles.contains("entrant"));
        assertTrue("Role list should contain 'organizer'", roles.contains("organizer"));
        assertTrue("Role list should contain 'admin'", roles.contains("admin"));
    }
}

