package com.example.cmput301project;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.cmput301project.view.MainActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.google.firebase.firestore.util.Assert.fail;
import static org.hamcrest.core.AllOf.allOf;

import android.content.Intent;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class UserStoryTests {

//    @Rule
//    public ActivityScenarioRule<MainActivity> activityRule =
//            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRuleNotification = GrantPermissionRule.grant(POST_NOTIFICATIONS);

    @Before
    public void setUp() {
        System.setProperty("firebase.test", "true");
    }

    @Test
    public void testNavigateToEntrantProfile() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest"); // Replace "uiTest" with the desired id
        ActivityScenario.launch(intent);

        // Wait for the activity to launch
        Thread.sleep(4000);
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
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest"); // Replace "uiTest" with the desired id
        ActivityScenario.launch(intent);

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
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest"); // Replace "uiTest" with the desired id
        ActivityScenario.launch(intent);

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
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest"); // Replace "uiTest" with the desired id
        ActivityScenario.launch(intent);

        // Start from the EntrantHomepageFragment
//        ActivityScenario.launch(MainActivity.class);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.btn_organizer))
                .perform(ViewActions.click());
    }

    // US 02.01.03 As an organizer, I want to create and manage my facility profile.
    @Test
    public void testFacilityProfile() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest"); // Replace "uiTest" with the desired id
        ActivityScenario.launch(intent);

        Thread.sleep(3000);

        editFacilityProfile("John Doe", "johndoe@example.com", "123-456-7890");

        Thread.sleep(2000);  // Wait for 2 seconds

        // Check if the warning dialog appears
        try {
            Thread.sleep(500); // Adjust timing if needed
            onView(withText("Invalid email form")).check(matches(isDisplayed()));
            onView(withText("OK")).perform(click());
        } catch (Exception e) {
            System.out.println("Warning dialog did not appear: " + e.getMessage());
        }

        // Save the changes
        onView(withId(R.id.btnEditSave)).perform(click());

        Thread.sleep(2000);

        // Verify the changes
        verifyFacilityProfile("John Doe", "johndoe@example.com", "123-456-7890");
    }

    @Test
    public void testNavigateToManageEvents() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest"); // Replace "uiTest" with the desired id
        ActivityScenario.launch(intent);

        Thread.sleep(3000);

        editFacilityProfile("John Doe", "johndoe@example.com", "123-456-7890");


        Thread.sleep(2000);

        onView(withId(R.id.btnEditSave)).perform(click());

        Thread.sleep(2000);

        onView(withContentDescription("Navigate up")) // Matches the toolbar's navigation button
                .perform(click());

        Thread.sleep(2000);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.btn_organizer))
                .perform(ViewActions.click());

        // Click on the button that navigates to the Manage Events view
        // Replace R.id.manage_events_button with the actual button ID that triggers the navigation
        Espresso.onView(ViewMatchers.withId(R.id.manageEventsButton))
                .perform(ViewActions.click());

        // Verify that the event list view is displayed
        Espresso.onView(ViewMatchers.withId(R.id.event_list))
                .check(matches(isDisplayed()));
    }
//    US 02.01.01 As an organizer I want to create a new event and generate a unique
//    promotional QR code that links to the event description and event poster in the app
//    US 02.01.02 As an organizer I want to store the generated QR code in my database
//    US 02.02.03 As an organizer I want to enable or disable the geolocation requirement for my event.
//    US 02.03.01 As an organizer I want to OPTIONALLY limit the number of entrants who can join my waiting list
//    US 02.04.01 As an organizer I want to upload an event poster to provide visual information to entrants
//    US 02.04.02 As an organizer I want to update an event poster to provide visual information to entrants
    @Test
    public void testOrganizerCreateEvent() throws InterruptedException, UiObjectNotFoundException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest"); // Replace "uiTest" with the desired id
        ActivityScenario.launch(intent);

        Thread.sleep(3000);

        editFacilityProfile("John Doe", "johndoe@example.com", "123-456-7890");

        Thread.sleep(2000);

        onView(withId(R.id.btnEditSave)).perform(click());

        Thread.sleep(2000);

        onView(withContentDescription("Navigate up")) // Matches the toolbar's navigation button
                .perform(click());

        Thread.sleep(2000);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.btn_organizer))
                .perform(ViewActions.click());

        // Click on the button that navigates to the Manage Events view
        // Replace R.id.manage_events_button with the actual button ID that triggers the navigation
        Espresso.onView(ViewMatchers.withId(R.id.manageEventsButton))
                .perform(ViewActions.click());

        Thread.sleep(2000);

        // Verify that the event list view is displayed
        Espresso.onView(ViewMatchers.withId(R.id.event_list))
                .check(matches(isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.add_event_button))
                .perform(ViewActions.click());

//        Thread.sleep(2000);

        onView(allOf(withId(R.id.event_name_edittext), isDisplayed()))
                .perform(clearText(), typeText("Test event"), closeSoftKeyboard());

        onView(withId(R.id.lottery_capacity_text))
                .perform(replaceText("0"), closeSoftKeyboard());

        // Set the start date directly
        onView(withId(R.id.start_date_text))
                .perform(replaceText("12/01/2024"), closeSoftKeyboard());

        // Set the end date directly
        onView(withId(R.id.end_date_text))
                .perform(replaceText("12/10/2024"), closeSoftKeyboard());

        onView(withId(R.id.save_event_button))
                .perform(click());

        Thread.sleep(3000);

        onView(withId(R.id.event_name))
                .check(matches(isDisplayed()));
    }


//    US 01.01.01 As an entrant, I want to join the waiting list for a specific event
//    US 01.01.02 As an entrant, I want to leave the waiting list for a specific event
//    US 01.05.02 As an entrant I want to be able to accept the invitation to register/sign up when chosen to participate in an event
//    US 01.05.03 As an entrant I want to be able to decline an invitation when chosen to participate in an event
//    US 01.08.01 As an entrant, I want to be warned before joining a waiting list that requires geolocation.
    @Test
    public void testEntrantEventViewFragment() throws InterruptedException {

        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Define a variable to hold the event ID
        final String[] validEventId = {null};

        CountDownLatch latch = new CountDownLatch(1); // To wait for async operation
        db.collection("organizers") // Top-level collection
                .document("uiTest") // Specific organizer document
                .collection("events") // Subcollection for events
                .limit(1) // Retrieve only one event
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        validEventId[0] = queryDocumentSnapshots.getDocuments().get(0).getString("id"); // Get the event ID from the field
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    latch.countDown();
                });

        latch.await(5, TimeUnit.SECONDS); // Wait for the query to complete

        if (validEventId[0] == null) {
            return;
        }

        // Create an Intent with the required arguments
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest");
        intent.putExtra("navigateTo", "entrantEventViewFragment");
        intent.putExtra("eventId", validEventId[0]); // Replace with a valid test Event ID

        // Launch the MainActivity with the Intent
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        Thread.sleep(3000);

        try {

            Espresso.onView(ViewMatchers.withId(R.id.join_class_button))
                    .check(matches(isEnabled()));

            Espresso.onView(ViewMatchers.withId(R.id.join_class_button))
                    .check(matches(isDisplayed()))
                    .perform(ViewActions.click());
        } catch (AssertionError e) {

            Espresso.onView(ViewMatchers.withId(R.id.leave_class_button))
                    .check(matches(isDisplayed()))
                    .perform(ViewActions.click());
        }

        Thread.sleep(3000);
    }

    private void editFacilityProfile(String name, String email, String phone) throws InterruptedException {
        // Navigate to the organizer homepage
        Espresso.onView(ViewMatchers.withId(R.id.btn_organizer))
                .perform(ViewActions.click());

        // Navigate to the Manage Facility view
        Espresso.onView(ViewMatchers.withId(R.id.manage_facility_btn))
                .perform(ViewActions.click());

        Thread.sleep(2000);  // Wait for 2 seconds to allow the view to render

        // Tap the Edit button to enable the fields for editing
        onView(withId(R.id.btnEditSave)).perform(click());

        Thread.sleep(2000);  // Wait for the view to render

        // Input the name
        onView(withId(R.id.entrant_profile_name))
                .perform(clearText(), typeText(name), closeSoftKeyboard());

        // Input the email
        onView(withId(R.id.entrant_profile_email))
                .perform(clearText(), typeText(email), closeSoftKeyboard());

        // Input the phone number (optional field)
        onView(withId(R.id.entrant_profile_phone))
                .perform(clearText(), typeText(phone), closeSoftKeyboard());
    }

    private void verifyFacilityProfile(String expectedName, String expectedEmail, String expectedPhone) {
        // Verify that the entered name, email, and phone are displayed in the fields
        onView(withId(R.id.entrant_profile_name)).check(matches(withText(expectedName)));
        onView(withId(R.id.entrant_profile_email)).check(matches(withText(expectedEmail)));
        onView(withId(R.id.entrant_profile_phone)).check(matches(withText(expectedPhone)));
    }

}