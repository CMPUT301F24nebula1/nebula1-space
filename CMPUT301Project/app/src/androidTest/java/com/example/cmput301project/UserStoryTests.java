package com.example.cmput301project;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;
import static org.hamcrest.Matchers.allOf;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;

import android.widget.EditText;

import com.example.cmput301project.view.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

// uses local firebase emulator to test
@RunWith(AndroidJUnit4.class)
public class UserStoryTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // US 02.01.03 As an organizer, I want to create and manage my facility profile.
    @Test
    public void manageFacilityProfile1() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject organizerBtn = device.findObject(new UiSelector().resourceId("com.example.cmput301project:id/btn_organizer"));
        UiObject facilityBtn = device.findObject(new UiSelector().resourceId("com.example.cmput301project:id/manage_facility_btn"));
        try {
            // Attempt to click using UiAutomator if the button is found
            if (organizerBtn.exists() && organizerBtn.isEnabled()) {
                organizerBtn.click();
            }
        } catch (Exception e) {
            throw new AssertionError("Could not perform click on organizer button using UiAutomator", e);
        }

        try {
            // Attempt to click using UiAutomator if the button is found
            if (facilityBtn.exists() && facilityBtn.isEnabled()) {
                facilityBtn.click();
            }
        } catch (Exception e) {
            throw new AssertionError("Could not perform click on manage facility button using UiAutomator", e);
        }

        onView(withId(R.id.btnEditSave)).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.profile_imageview)).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.edit_profile_picture_button)).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.entrant_profile_name)).check(ViewAssertions.matches(isDisplayed()));
        onView(withId(R.id.entrant_profile_email)).check(ViewAssertions.matches(isDisplayed()));
//        onView(withId(R.id.entrant_profile_email)).check(matches(withText("t")));
        onView(withId(R.id.entrant_profile_phone)).check(ViewAssertions.matches(isDisplayed()));
    }

    @Test
    public void manageFacilityProfile2() throws InterruptedException {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject organizerBtn = device.findObject(new UiSelector().resourceId("com.example.cmput301project:id/btn_organizer"));
        UiObject facilityBtn = device.findObject(new UiSelector().resourceId("com.example.cmput301project:id/manage_facility_btn"));

        try {
            // Attempt to click using UiAutomator if the button is found
            if (organizerBtn.exists() && organizerBtn.isEnabled()) {
                organizerBtn.click();
            }
        } catch (Exception e) {
            throw new AssertionError("Could not perform click on organizer button using UiAutomator", e);
        }

        try {
            // Attempt to click using UiAutomator if the button is found
            if (facilityBtn.exists() && facilityBtn.isEnabled()) {
                facilityBtn.click();
            }
        } catch (Exception e) {
            throw new AssertionError("Could not perform click on manage facility button using UiAutomator", e);
        }

        Thread.sleep(2000);  // Wait for 2 seconds to allow view to render

        // Tap the Edit button to enable the fields for editing
        onView(withId(R.id.btnEditSave)).perform(click());

        Thread.sleep(1000);  // Wait for 2 seconds to allow view to render

        // Input the name
        onView(withId(R.id.entrant_profile_name))
                .perform(clearText(), typeText("John Doe"), closeSoftKeyboard());

        // Input the email
        onView(withId(R.id.entrant_profile_email))
                .perform(clearText(), typeText("johndoe@example.com"), closeSoftKeyboard());

        // Optional: Input the phone number (optional field)
        onView(withId(R.id.entrant_profile_phone))
                .perform(clearText(), typeText("123-456-7890"), closeSoftKeyboard());

        Thread.sleep(2000);  // Wait for 2 seconds
        // Check if the warning dialog appears
        try {
            // Wait briefly for the dialog to appear
            Thread.sleep(500); // Adjust timing if needed

            // Verify that the warning dialog with specific text is displayed
            onView(withText("Invalid email form")).check(ViewAssertions.matches(isDisplayed()));

            // Dismiss the dialog
            onView(withText("OK")).perform(click());
        } catch (Exception e) {
            // Handle case where dialog may not appear, if no warning dialog is expected for valid cases
            System.out.println("Warning dialog did not appear: " + e.getMessage());
        }
        // Click the save button
        onView(withId(R.id.btnEditSave)).perform(click());

        // Verify that the entered name, email, and phone are displayed in the fields
        onView(withId(R.id.entrant_profile_name)).check(ViewAssertions.matches(withText("John Doe")));
        onView(withId(R.id.entrant_profile_email)).check(ViewAssertions.matches(withText("johndoe@example.com")));
        onView(withId(R.id.entrant_profile_phone)).check(ViewAssertions.matches(withText("123-456-7890")));
    }

    @Test
    public void testAddEvent() throws InterruptedException {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject organizerBtn = device.findObject(new UiSelector().resourceId("com.example.cmput301project:id/btn_organizer"));
        UiObject facilityBtn = device.findObject(new UiSelector().resourceId("com.example.cmput301project:id/manage_facility_btn"));
        UiObject manageEventBtn = device.findObject(new UiSelector().resourceId("com.example.cmput301project:id/manageEventsButton"));
        UiObject addBtn = device.findObject(new UiSelector().resourceId("com.example.cmput301project:id/add_event_button"));
        try {
            // Attempt to click using UiAutomator if the button is found
            if (organizerBtn.exists() && organizerBtn.isEnabled()) {
                organizerBtn.click();
            }
        } catch (Exception e) {
            throw new AssertionError("Could not perform click on organizer button using UiAutomator", e);
        }
        try {
            // Attempt to click using UiAutomator if the button is found
            if (manageEventBtn.exists() && manageEventBtn.isEnabled()) {
                manageEventBtn.click();
            }
            else if (manageEventBtn.exists() && !manageEventBtn.isEnabled()) {
                assertEquals(facilityBtn.getText(), "Create Facility");
            }
        } catch (Exception e) {
            throw new AssertionError("Could not perform click on manage event button using UiAutomator", e);
        }
        try {
            // Attempt to click using UiAutomator if the button is found
            if (addBtn.exists() && addBtn.isEnabled()) {
                addBtn.click();
            }
            else if (addBtn.exists() && !addBtn.isEnabled()) {
                assertEquals(facilityBtn.getText(), "Create Facility");
            }
        } catch (Exception e) {
            throw new AssertionError("Could not perform click on add event button using UiAutomator", e);
        }

        Thread.sleep(2000);  // Wait for 2 seconds to allow view to render

        // Input the name
        onView(allOf(isDescendantOfA(withId(R.id.event_name)), isAssignableFrom(EditText.class)))
                .perform(clearText(), typeText("CMPUT 301"), closeSoftKeyboard());

        // input the description
        onView(allOf(isDescendantOfA(withId(R.id.event_description)), isAssignableFrom(EditText.class)))
                .perform(clearText(), typeText("Fall 24"), closeSoftKeyboard());

//        onView(allOf(isDescendantOfA(withId(R.id.lottery_starts_date)), isAssignableFrom(EditText.class)))
//                .perform(clearText(), typeText("01/09/2024"), closeSoftKeyboard());
//
//        onView(allOf(isDescendantOfA(withId(R.id.lottery_ends_date)), isAssignableFrom(EditText.class)))
//                .perform(clearText(), typeText("12/12/2024"), closeSoftKeyboard());

        onView(allOf(isDescendantOfA(withId(R.id.lottery_capacity)), isAssignableFrom(EditText.class)))
                .perform(clearText(), typeText("324"), closeSoftKeyboard());




    }


    // US 01.01.01 As an entrant, I want to join the waiting list for a specific event
//    @Test
//    public void testJoin() {
//        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
////        UiObject profileButton = device.findObject(new UiSelector().resourceId("com.example.cmput301project:id/profile_button"));
//
//        // Verify the initial state of the buttons
//        onView(withId(R.id.join_class_button))
//                .check(matches(isEnabled()));
//        onView(withId(R.id.leave_class_button))
//                .check(matches(not(isEnabled())));
//
//        // Perform a click on the "Join" button
//        onView(withId(R.id.joinClassButton)).perform(click());
//
//        // Check that the "Join" button is now disabled and the "Leave" button is enabled
//        onView(withId(R.id.join_class_button))
//                .check(matches(not(isEnabled())));
//        onView(withId(R.id.leaveClassButton))
//                .check(matches(isEnabled()));
//
//        // Verify that a toast is displayed confirming the joining of the event
////        onView(withText("You joined the waiting list!"))
////                .inRoot(new ToastMatcher())
////                .check(matches(isDisplayed()));
//
//        // Verify that the mockEntrant is added to the event's waitlist
//        assertTrue(mockEntrant.getWaitlistEventIds().contains(mockEvent.getId()));
//
//
//    }
}
