package com.example.cmput301project;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.cmput301project.view.MainActivity;
import com.google.android.material.card.MaterialCardView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

@RunWith(AndroidJUnit4.class)
public class EntrantProfileFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRuleNotification = GrantPermissionRule.grant(POST_NOTIFICATIONS);

    @Test
    public void testNavigateToEntrantProfile() throws InterruptedException {
        // Use UiAutomator to click the button directly
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject profileButton = device.findObject(new UiSelector().resourceId("com.example.cmput301project:id/profile_button"));

        Thread.sleep(4000);

        try {
            // Attempt to click using UiAutomator if the button is found
            if (profileButton.exists() && profileButton.isEnabled()) {
                profileButton.click();
            }
        } catch (Exception e) {
            throw new AssertionError("Could not perform click on profile_button using UiAutomator", e);
        }

        // Verify elements on entrant_profile.xml
        onView(withId(R.id.btnEditSave)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_imageview)).check(matches(isDisplayed()));
        onView(withId(R.id.entrant_profile_name)).check(matches(isDisplayed()));
        onView(withId(R.id.entrant_profile_email)).check(matches(isDisplayed()));
//        onView(withId(R.id.entrant_profile_email)).check(matches(withText("t")));
        onView(withId(R.id.entrant_profile_phone)).check(matches(isDisplayed()));
    }

    // US 01.02.01 As an entrant, I want to provide my personal information such as name,
    // email and optional phone number in the app.
    // US 01.02.02 As an entrant I want to update information such as name,
    // email and contact information on my profile.
    // US 01.03.01 As an entrant I want to upload a profile picture for a more personalized experience
    // US 01.03.02 As an entrant I want remove profile picture if need be
    // US 01.03.03 As an entrant I want my profile picture to be deterministically generated from
    // my profile name if I haven't uploaded a profile image yet.
    @Test
    public void testEntrantProvidesPersonalInfo() throws InterruptedException {
        // Use UiAutomator to click the button directly
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject profileButton = device.findObject(new UiSelector().resourceId("com.example.cmput301project:id/profile_button"));

        Thread.sleep(2000);

        try {
            // Attempt to click using UiAutomator if the button is found
            if (profileButton.exists() && profileButton.isEnabled()) {
                profileButton.click();
            }
        } catch (Exception e) {
            throw new AssertionError("Could not perform click on profile_button using UiAutomator", e);
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
            onView(withText("Invalid email form")).check(matches(isDisplayed()));

            // Dismiss the dialog
            onView(withText("OK")).perform(click());
        } catch (Exception e) {
            // Handle case where dialog may not appear, if no warning dialog is expected for valid cases
            System.out.println("Warning dialog did not appear: " + e.getMessage());
        }
        // Click the save button
        onView(withId(R.id.btnEditSave)).perform(click());
        Thread.sleep(2000);

        // Verify that the entered name, email, and phone are displayed in the fields
        onView(withId(R.id.entrant_profile_name)).check(matches(withText("John Doe")));
        onView(withId(R.id.entrant_profile_email)).check(matches(withText("johndoe@example.com")));
        onView(withId(R.id.entrant_profile_phone)).check(matches(withText("123-456-7890")));
    }

    @Test
    public void testEntrantReceiveNotification() throws InterruptedException {
        // Use UiAutomator to click the button directly
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject profileButton = device.findObject(new UiSelector().resourceId("com.example.cmput301project:id/profile_button"));

        try {
            // Attempt to click using UiAutomator if the button is found
            if (profileButton.exists() && profileButton.isEnabled()) {
                profileButton.click();
            }
        } catch (Exception e) {
            throw new AssertionError("Could not perform click on profile_button using UiAutomator", e);
        }

        Thread.sleep(2000);  // Wait for 2 seconds to allow view to render

        onView(allOf(withId(R.id.btn_notification), isDisplayed())).perform(click());

        // Tap the Notification button
//        onView(withId(R.id.btn_notification)).perform(click());
    }

    @Test
    public void testNavigateToOrganizerHomepage() {
        // Start from the EntrantHomepageFragment
        ActivityScenario.launch(MainActivity.class);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.btn_organizer))
                .perform(ViewActions.click());
    }

//    @Test
//    public void testNavigateToManageEvents() {
//        // Start from the EntrantHomepageFragment
//        ActivityScenario.launch(MainActivity.class);
//
//        // Click on the Organizer button in the toggle group
//        Espresso.onView(ViewMatchers.withId(R.id.btn_organizer))
//                .perform(ViewActions.click());
//
//        // Click on the button that navigates to the Manage Events view
//        // Replace R.id.manage_events_button with the actual button ID that triggers the navigation
//        Espresso.onView(ViewMatchers.withId(R.id.manageEventsButton))
//                .perform(ViewActions.click());
//
//        // Verify that the event list view is displayed
//        Espresso.onView(ViewMatchers.withId(R.id.event_list))
//                .check(matches(isDisplayed()));
//
//        // Click the first item in the event list
//        Espresso.onView(ViewMatchers.withId(R.id.event_list))
//                .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));
//    }
}