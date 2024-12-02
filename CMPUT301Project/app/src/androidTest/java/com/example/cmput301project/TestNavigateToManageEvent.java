package com.example.cmput301project;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.cmput301project.controller.QRCodeGenerator;
import com.example.cmput301project.controller.UserController;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.example.cmput301project.model.User;
import com.example.cmput301project.view.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.junit.After;
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
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.google.firebase.firestore.util.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;


@RunWith(AndroidJUnit4.class)
public class TestNavigateToManageEvent {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    );

    @Before
    public void setUp() throws InterruptedException {
        System.setProperty("firebase.test", "true");
        createTestUsersAndEntrants();
    }

    private ActivityScenario<MainActivity> scenario;

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close(); // Close the current scenario to clean up
            scenario = null;
        }
    }

    @Test
    public void testNavigateToManageEvents() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest"); // Replace "uiTest" with the desired id
        scenario =  ActivityScenario.launch(intent);

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

    public void clearFirebaseEmulatorData() {
        OkHttpClient client = new OkHttpClient();

        // emulator's URL and port
        String url = "http://10.0.2.2:8080/emulator/v1/projects/cmput301test-cdac2/databases/(default)/documents";

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e("FirebaseReset", "Failed to clear Firebase Emulator: " + response.message());
            }
        } catch (Exception e) {
            Log.e("FirebaseReset", "Error clearing Firebase Emulator", e);
        }
    }

    private void createTestUsersAndEntrants() throws InterruptedException {

        // CountDownLatch for clearing Firestore Emulator data
        CountDownLatch clearLatch = new CountDownLatch(1);

        // Clear Firestore Emulator data in a separate thread
        new Thread(() -> {
            clearFirebaseEmulatorData();
            clearLatch.countDown(); // Signal completion of the clearing operation
        }).start();

        // Wait up to 10 seconds for clearing operation to complete
        if (!clearLatch.await(10, TimeUnit.SECONDS)) {
            Log.e("firebase rest", "Clearing Firebase Emulator data timed out.");
            return; // Exit test if clearing fails
        }

        // Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create test users and entrants
        List<User> testUsers = new ArrayList<>();
        testUsers.add(new User("uiTest"));
        testUsers.add(new User("test1"));
        testUsers.add(new User("test2"));
        testUsers.add(new User("test3"));
        testUsers.add(new User("test4"));
        testUsers.add(new User("test5"));
        testUsers.add(new User("test6"));

        List<Entrant> testEntrants = new ArrayList<>();
        testEntrants.add(new Entrant("uiTest"));
        testEntrants.add(new Entrant("test1"));
        testEntrants.add(new Entrant("test2"));
        testEntrants.add(new Entrant("test3"));
        testEntrants.add(new Entrant("test4"));
        testEntrants.add(new Entrant("test5"));
        testEntrants.add(new Entrant("test6"));

        // Latch for synchronization
        CountDownLatch latch = new CountDownLatch(testUsers.size() + testEntrants.size());

        // Add users inline
        for (User user : testUsers) {
//            UserController.updateUserRole(user.getId(), "organizer");
//            UserController.updateUserRole(user.getId(), "admin");
            user.addAdmin();
            user.addOrganizerRole();
            db.collection("users").document(user.getId())
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Test", "User added: " + user.getId());
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Test", "Failed to add user: " + user.getId(), e);
                        latch.countDown();
                    });
        }

        int i = 0;
        // Add entrants inline
        for (Entrant entrant : testEntrants) {
            entrant.setName("test" + String.valueOf(i++));
            entrant.setEmail(entrant.getName() + "@email.com");
            db.collection("entrants").document(entrant.getId())
                    .set(entrant)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Test", "Entrant added: " + entrant.getName());
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Test", "Failed to add entrant: " + entrant.getName(), e);
                        latch.countDown();
                    });
        }

        // create an organizer
        Organizer organizer = new Organizer("uiTest");
        organizer.setName("UI Test Facility");
        organizer.setEmail("facility@email.com");
        saveOrganizerToDatabase(organizer);


        for (i = 0; i < 6; i++) {
            Event event = new Event("Test event" + String.valueOf(i));
            event.setStartDate("30/12/2025");
            event.setEndDate("30/12/2025");
            event.setLimit(0);
            if (i == 1) {
                event.setName("Geolocation enabled");
                event.setRequiresGeolocation(true);
            }
            if (i == 2) {
                addEvent("uiTest", event, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("ui test add event", "event added");
                        for (Entrant entrant : testEntrants) {
                            addEntrantToWaitlist(event, entrant, db);
                            addToEventWaitingList(event, entrant.getId());
                            sendNotification("You are in the waiting list", entrant.getId(), event, "SELECTED");
                        }
                    }
                }, e -> Log.d("ui test add event", "event added fails."));
            } else {
                addEvent("uiTest", event, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("ui test add event", "event added");
                        for (Entrant entrant : testEntrants) {
                            addEntrantToWaitlist(event, entrant, db);
                            addToEventWaitingList(event, entrant.getId());
                            sendNotification("You are in the waiting list", entrant.getId(), event, "WAITING");
                        }
                    }
                }, e -> Log.d("ui test add event", "event added fails."));
            }
        }

        // Wait for all operations to finish
        latch.await();

        Thread.sleep(7000);
        Log.d("Test", "All users and entrants have been created.");
    }

    public void sendNotification(String notification, String entrantId, Event event, String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("isRead", false); // or "true" if the notification is read
        notificationData.put("message", notification);
        notificationData.put("eventId", event.getId());
        notificationData.put("timestamp", FieldValue.serverTimestamp());
        notificationData.put("status", status);
        notificationData.put("title", event.getName());

        db.collection("entrants")
                .document(entrantId) // Set the document ID to entrant.getId()
                .collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(aVoid -> {
                    // Successfully written to Firestore
                    Log.d("Firestore", "Notification successfully written for entrant ID: " + entrantId);
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Log.e("FirestoreError", "Error writing notification", e);
                });
    }

    public void saveOrganizerToDatabase(Organizer organizer) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> organizerData = new HashMap<>();
        organizerData.put("name", organizer.getName());
        organizerData.put("email", organizer.getEmail());
        organizerData.put("phone", organizer.getPhone());
        organizerData.put("id", organizer.getId());

        db.collection("organizers")
                .document(organizer.getId())
                .set(organizerData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("saveEntrantToDatabase", "Entrant data updated successfully in Firebase");
                })
                .addOnFailureListener(e -> {
                    Log.e("saveEntrantToDatabase", "Failed to update entrant data in Firebase", e);
                });
        Log.d("save entrant profile", organizer.toString());
    }

    private void addEntrantToWaitlist(Event event, Entrant entrant, FirebaseFirestore db) {
        Map<String, Object> waitlistData = new HashMap<>();
        waitlistData.put("eventId", event.getId());
        waitlistData.put("status", "WAITING");

        db.collection("entrants")
                .document(entrant.getId())  // Assuming entrant has a unique ID
                .collection("entrantWaitList")
                .document(event.getId())
                .set(waitlistData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("addEntrantToWaitlist", "Entrant successfully added to event waitlist.");
                })
                .addOnFailureListener(e -> {
                    Log.e("addEntrantToWaitlist", "Failed to add entrant to event waitlist: " + e.getMessage(), e);
                });
    }

    private void addToEventWaitingList(Event event, String entrantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        event.getWaitlistEntrantIds().add(entrantId);

        db.collection("organizers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot organizerDoc : queryDocumentSnapshots) {
                        String organizerId = organizerDoc.getId();

                        // Get the events subcollection for each organizer
                        db.collection("organizers")
                                .document(organizerId)
                                .collection("events")
                                .document(event.getId())
                                .get()
                                .addOnSuccessListener(eventDoc -> {
                                    if (eventDoc.exists()) {
                                        // If the event with targetEventId exists, add the new user to the userId subcollection
                                        db.collection("organizers")
                                                .document(organizerId)
                                                .collection("events")
                                                .document(event.getId())
                                                .collection("userId")
                                                .document(entrantId)
                                                .set(new HashMap<String, Object>() {{
                                                    put("userId", entrantId);
                                                }}) // You can add any field-value pairs here if necessary
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("addUserToEvent", "User ID added successfully to the event: " + event.getId());
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("addUserToEvent", "Failed to add user ID to the event: " + e.getMessage(), e);
                                                });
                                    } else {
                                        Log.d("addUserToEvent", "Event with ID " + event.getId() + " not found in organizer " + organizerId);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("addUserToEvent", "Failed to retrieve event: " + e.getMessage(), e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("addUserToEvent", "Failed to retrieve organizers: " + e.getMessage(), e);
                });
    }

    private void addEvent(String id, Event event, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("organizers")
                .document(id)
                .collection("events");

        eventsCollection.document(event.getId())
                .set(event) // Save the event object first
                .addOnSuccessListener(eventVoid -> {
                    // Now update the 'timestamp' field with the server timestamp
                    eventsCollection.document(event.getId())
                            .update("timestamp", FieldValue.serverTimestamp()) // Update the timestamp field
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Firestore", "Event and timestamp successfully added: " + event.getId());
                                successListener.onSuccess(null); // Call the success listener
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error updating timestamp for event: " + event.getId(), e);
                                failureListener.onFailure(e); // Call the failure listener
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding event: " + event.getId(), e);
                    failureListener.onFailure(e); // Call the failure listener
                });
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
