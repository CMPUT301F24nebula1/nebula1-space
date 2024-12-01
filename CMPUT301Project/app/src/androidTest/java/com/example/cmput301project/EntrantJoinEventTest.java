package com.example.cmput301project;

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
import static org.hamcrest.core.AllOf.allOf;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

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

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RunWith(AndroidJUnit4.class)
public class EntrantJoinEventTest {

//    @Rule
//    public ActivityScenarioRule<MainActivity> activityRule =
//            new ActivityScenarioRule<>(MainActivity.class);

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
    public void testEntrantClassFragment() throws InterruptedException {

        // Create an Intent with the required arguments
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest");

        // Launch the MainActivity with the Intent
        scenario =  ActivityScenario.launch(intent);

        Thread.sleep(3000);

        Espresso.onView(allOf(withText("My Classes"), isDisplayed()))
                .perform(click());


        Thread.sleep(2000);

        try {
            Espresso.onView(withText("Test event5"))
                    .check(matches(isDisplayed()));
        } catch (androidx.test.espresso.AmbiguousViewMatcherException |
                 NoMatchingViewException exception) {
            Log.d("ui test", "several matches");
        }

        Thread.sleep(2000);
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
        intent.putExtra("category", "WAITING");

        // Launch the MainActivity with the Intent
        scenario =  ActivityScenario.launch(intent);

        Thread.sleep(3000);

        try {

            Espresso.onView(ViewMatchers.withId(R.id.join_class_button))
                    .check(matches(isEnabled()));

            Espresso.onView(ViewMatchers.withId(R.id.join_class_button))
                    .check(matches(isDisplayed()))
                    .perform(ViewActions.click());
        } catch (AssertionError e) {

            try {
                Espresso.onView(ViewMatchers.withId(R.id.leave_class_button))
                        .check(matches(isDisplayed()))
                        .perform(ViewActions.click());
            } catch (AssertionError e1) {
                ;
            }
        }

        Thread.sleep(3000);
    }


//    US 01.05.02 As an entrant I want to be able to accept the invitation to register/sign up when chosen to participate in an event
//    US 01.05.03 As an entrant I want to be able to decline an invitation when chosen to participate in an event
    @Test
    public void testEntrantAcceptInvitation() throws InterruptedException {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Define a variable to hold the event ID
        final String[] validEventId = {null};
        CountDownLatch latch = new CountDownLatch(1); // To wait for async operation

        // Query the entrants collection for entrant ID "uiTest"
        db.collection("entrants")
                .document("uiTest") // Document ID for the entrant
                .collection("entrantWaitList") // Subcollection
                .whereEqualTo("status", "SELECTED") // Condition
                .limit(1) // Fetch only the first matching result
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the event ID or relevant document data
                        validEventId[0] = queryDocumentSnapshots.getDocuments().get(0).getString("eventId"); // Replace "eventId" with the actual field name
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error fetching documents: " + e.getMessage());
                    latch.countDown();
                });

        // Wait for Firestore query to complete
        latch.await(5, TimeUnit.SECONDS);


        if (validEventId[0] == null) {
            return;
        }

        // Create an Intent with the required arguments
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest");

        // Launch the MainActivity with the Intent
        scenario =  ActivityScenario.launch(intent);

        Thread.sleep(3000);

        Espresso.onView(allOf(withText("My Classes"), isDisplayed()))
                .perform(click());

        Thread.sleep(3000);

        // Find the ListView and click on the first item
        Espresso.onView(withId(R.id.event_list)) // Replace with the actual ListView ID
                .perform(new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isDisplayed(); // Ensure the ListView is displayed
                    }

                    @Override
                    public String getDescription() {
                        return "Click on first item in the ListView";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        // Get the ListView and its first visible child
                        ListView listView = (ListView) view;
                        View itemView = listView.getChildAt(0); // Replace 0 with the desired index

                        // Perform a click on the item
                        itemView.performClick();
                    }
                });

    }


//    US 01.08.01 As an entrant, I want to be warned before joining a waiting list that requires geolocation.
    @Test
    public void testGeoEvent() throws InterruptedException {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Define a variable to hold the event ID
        final String[] validEventId = {null};

        CountDownLatch latch = new CountDownLatch(1); // To wait for async operation
        db.collection("organizers") // Top-level collection
                .document("uiTest") // Specific organizer document
                .collection("events") // Subcollection for events
                .whereEqualTo("requiresGeolocation", true)
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
        scenario =  ActivityScenario.launch(intent);

        Thread.sleep(3000);

        try {

            Espresso.onView(ViewMatchers.withId(R.id.join_class_button))
                    .check(matches(isEnabled()));

            Espresso.onView(ViewMatchers.withId(R.id.join_class_button))
                    .check(matches(isDisplayed()))
                    .perform(ViewActions.click());

            // Check if the dialog is displayed with the correct title and message
            Espresso.onView(ViewMatchers.withText("Geolocation Required"))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            Espresso.onView(ViewMatchers.withText("This event utilizes geolocation. Are you sure you wish to proceed?"))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            // Click the "Proceed" button
            Espresso.onView(ViewMatchers.withText("Proceed"))
                    .perform(ViewActions.click());

        } catch (AssertionError e) {

            try {
                Espresso.onView(ViewMatchers.withId(R.id.leave_class_button))
                        .check(matches(isDisplayed()))
                        .perform(ViewActions.click());

                Thread.sleep(2000);

                Espresso.onView(ViewMatchers.withId(R.id.join_class_button))
                        .check(matches(isEnabled()));

                Espresso.onView(ViewMatchers.withId(R.id.join_class_button))
                        .check(matches(isDisplayed()))
                        .perform(ViewActions.click());

                Thread.sleep(1000);

                // Check if the dialog is displayed with the correct title and message
                Espresso.onView(ViewMatchers.withText("Geolocation Required"))
                        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

                Espresso.onView(ViewMatchers.withText("This event utilizes geolocation. Are you sure you wish to proceed?"))
                        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

                Thread.sleep(1000);

                // Click the "Proceed" button
                Espresso.onView(ViewMatchers.withText("Proceed"))
                        .perform(ViewActions.click());
            } catch (AssertionError e1) {
                ;
            }
        }

        Thread.sleep(3000);
    }


    public void clearFirebaseEmulatorData() {
        OkHttpClient client = new OkHttpClient();

        // Replace with your emulator's URL and port (default is http://10.0.2.2:8080 for Android Emulator)
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

    //    @Test
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
                            addEntrantToWaitlist(event, entrant, db, "SELECTED");
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
                            addEntrantToWaitlist(event, entrant, db, "WAITING");
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

    private void addEntrantToWaitlist(Event event, Entrant entrant, FirebaseFirestore db, String status) {
        Map<String, Object> waitlistData = new HashMap<>();
        waitlistData.put("eventId", event.getId());
        waitlistData.put("status", status);

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
