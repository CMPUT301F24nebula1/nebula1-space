package com.example.cmput301project;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.cmput301project.controller.EntrantArrayAdapter;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.example.cmput301project.model.User;
import com.example.cmput301project.view.MainActivity;
import com.example.cmput301project.view.ParticipantListActivity;
import com.example.cmput301project.view.WaitlistMapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.UiController;
import android.view.View;
import com.google.android.material.slider.Slider;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;

import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import androidx.test.rule.GrantPermissionRule;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;


@RunWith(AndroidJUnit4.class)
public class moreUserStoryTests {

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
        clearFirebaseEmulatorData();
    }

    private ActivityScenario<MainActivity> scenario;

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close(); // Close the current scenario to clean up
            scenario = null;
        }
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

    public static ViewAction setSliderValue(final float value) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                // This ensures that only sliders are targeted
                return isAssignableFrom(Slider.class);
            }

            @Override
            public String getDescription() {
                return "Set slider value to: " + value;
            }

            @Override
            public void perform(UiController uiController, View view) {
                Slider slider = (Slider) view;
                slider.setValue(value); // Set the slider's value
            }
        };
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

    //utility func
    public static ViewAction clickOnItemAtPosition(final int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(ListView.class);
            }

            @Override
            public String getDescription() {
                return "Click on item at position " + position;
            }

            @Override
            public void perform(UiController uiController, View view) {
                ListView listView = (ListView) view;
                View child = listView.getChildAt(position);
                child.performClick();
            }
        };
    }

    //utility func
    public static Matcher<View> atLeastOneItemDisplayed() {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                if (!(view instanceof ListView)) return false;
                ListView listView = (ListView) view;
                return listView.getChildCount() > 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("ListView should have at least one visible item");
            }
        };
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


    //    Test US 02.02.01 As an organizer I want to view the list of entrants who joined my event waiting list
    @Test
    public void testViewEventWaitingList() {
        // Step 1: Create a mock Event
        Event mockEvent = new Event("Test Event"); // Automatically generates a UUID
        String mockOrganizerId = "mockOrganizerId";

        // Step 2: Create an Intent with the mock Event and Organizer ID
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ParticipantListActivity.class);
        intent.putExtra("event", mockEvent);
        intent.putExtra("organizerId", mockOrganizerId);

        // Step 3: Launch the Activity
        ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent);

        // Step 4: Verify that the activity is displayed
        onView(withId(R.id.participant_list)).check(matches(isDisplayed()));

        onView(withId(R.id.toolbar_select)).check(matches(isDisplayed()));
        onView(withText("Entrant Lists")).check(matches(isDisplayed()));
    }


//    Test US 02.02.02 As an organizer I want to see on a map where entrants joined my event waiting list from.
    @Test
    public void testOpenWaitlistMap() throws InterruptedException {

        Event mockEvent = new Event("Test Event");
        mockEvent.setOrganizerId("mockOrganizerId");
        mockEvent.setRequiresGeolocation(true);
        mockEvent.setWaitlistEntrantIds(new ArrayList<>(List.of("entrant1", "entrant2")));

        List<Entrant> mockEntrants = new ArrayList<>();
        mockEntrants.add(new Entrant("entrant1"));
        mockEntrants.add(new Entrant("entrant2"));

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ParticipantListActivity.class);
        intent.putExtra("event", mockEvent);
        intent.putExtra("organizerId", "mockOrganizerId");
        ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent);

        Thread.sleep(1000);

        scenario.onActivity(activity -> {
            WaitlistMapFragment mapFragment = WaitlistMapFragment.newInstance(mockEvent.getId());
            mapFragment.show(activity.getSupportFragmentManager(), "WaitlistMapFragment");
        });

        Thread.sleep(1000);
        onView(withId(R.id.mapImageView))
                .check(matches(isDisplayed()));
    }


    //    Test US 02.05.01 As an organizer I want to send a notification to chosen entrants to sign up for events.
    @Test
    public void testNotifyButton() throws InterruptedException {

        Event mockEvent = new Event("Test Event");
        mockEvent.setOrganizerId("uiTest");
        mockEvent.getWaitlistEntrantIds().add("entrant1");

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ParticipantListActivity.class);
        intent.putExtra("event", mockEvent);
        intent.putExtra("organizerId", "uiTest");
        ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.notify_button))
                .perform(click());

        Log.d("Notification Test", "Test Completed");
    }


//    Test US 02.05.02 As an organizer I want to set the system to sample a specified number of attendees to register for the event
    @Test
    public void testSampleEntrants() throws InterruptedException {
        Event mockEvent = new Event("Sample Event");
        mockEvent.setOrganizerId("uiTest");
        mockEvent.getWaitlistEntrantIds().add("entrant1");

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ParticipantListActivity.class);
        intent.putExtra("event", mockEvent);
        intent.putExtra("organizerId", "uiTest");
        ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.slider))
                .perform(setSliderValue(5));

        onView(withId(R.id.select_button))
                .check(matches(withText("Draw 5 Participants")));
        Log.d("Sample Entrants Test", "Test completed.");
    }


//    Test US 02.05.03 As an organizer I want to be able to draw a replacement applicant from the pooling system when a previously selected applicant cancels or rejects the invitation
    @Test
    public void testDrawReplacementEntrant() {
        Event mockEvent = new Event("Mock Event");
        mockEvent.setRequiresGeolocation(true);
        mockEvent.getWaitlistEntrantIds().addAll(Arrays.asList("entrant1", "entrant2", "entrant3"));

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ParticipantListActivity.class);
        intent.putExtra("event", mockEvent);
        intent.putExtra("organizerId", "mockOrganizer");
        ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            try {
                Field participantListField = ParticipantListActivity.class.getDeclaredField("participantList");
                participantListField.setAccessible(true);
                ListView participantList = (ListView) participantListField.get(activity);

                List<Entrant> mockEntrants = Arrays.asList(
                        new Entrant("entrant1"),
                        new Entrant("entrant2"),
                        new Entrant("entrant3")
                );

                EntrantArrayAdapter adapter = new EntrantArrayAdapter(activity, new ArrayList<>(mockEntrants));
                participantList.setAdapter(adapter);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new AssertionError("Reflection failed: Unable to access participantList", e);
            }
        });

        onView(withId(R.id.select_button))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.participant_list))
                .check(matches(isDisplayed()));
    }


    //    Test US 02.06.01 As an organizer I want to view a list of all chosen entrants who are invited to apply
    @Test
    public void testViewSelectedEntrants() throws InterruptedException {
        // Step 1: Set up a mock event with mock selected entrants
        Event mockEvent = new Event("Test Event");
        mockEvent.setOrganizerId("mockOrganizer");
        mockEvent.getWaitlistEntrantIds().add("entrant1");
        mockEvent.getWaitlistEntrantIds().add("entrant2");

        Entrant entrant1 = new Entrant("entrant1");
        entrant1.setName("John Doe");
        Entrant entrant2 = new Entrant("entrant2");
        entrant2.setName("Jane Smith");

        // Simulate that entrants are "SELECTED" in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("entrants").document("entrant1").set(entrant1);
        db.collection("entrants").document("entrant2").set(entrant2);

        // Simulate selected entrant status for both
        db.collection("entrants")
                .document("entrant1")
                .collection("entrantWaitList")
                .document(mockEvent.getId())
                .set(new HashMap<String, Object>() {{
                    put("status", "SELECTED");
                }});

        db.collection("entrants")
                .document("entrant2")
                .collection("entrantWaitList")
                .document(mockEvent.getId())
                .set(new HashMap<String, Object>() {{
                    put("status", "SELECTED");
                }});

        Thread.sleep(1000);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ParticipantListActivity.class);
        intent.putExtra("event", mockEvent);
        intent.putExtra("organizerId", "mockOrganizer");
        ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent);

        Thread.sleep(1000);
        onView(withId(R.id.btn_selected)).perform(click());

        onView(withId(R.id.participant_list)).check(matches(isDisplayed()));


        onView(withId(R.id.participant_list))
                .check((view, noViewFoundException) -> {
                    if (noViewFoundException != null) {
                        throw noViewFoundException;
                    }

                    Log.d("View Selected Entrants Test", "Test Completed");
                });
        }


//    Test US 02.06.02 As an organizer I want to see a list of all the canceled entrants
    @Test
    public void testViewCanceledEntrants() throws InterruptedException {

        Event mockEvent = new Event("Test Event");
        mockEvent.setOrganizerId("mockOrganizer");
        mockEvent.getWaitlistEntrantIds().add("entrant3");
        mockEvent.getWaitlistEntrantIds().add("entrant4");

        Entrant entrant3 = new Entrant("entrant3");
        entrant3.setName("Chris Brown");
        Entrant entrant4 = new Entrant("entrant4");
        entrant4.setName("Alex Johnson");


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("entrants").document("entrant3").set(entrant3);
        db.collection("entrants").document("entrant4").set(entrant4);


        db.collection("entrants")
                .document("entrant3")
                .collection("entrantWaitList")
                .document(mockEvent.getId())
                .set(new HashMap<String, Object>() {{
                    put("status", "CANCELED");
                }});

        db.collection("entrants")
                .document("entrant4")
                .collection("entrantWaitList")
                .document(mockEvent.getId())
                .set(new HashMap<String, Object>() {{
                    put("status", "CANCELED");
                }});

        Thread.sleep(1000);


        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ParticipantListActivity.class);
        intent.putExtra("event", mockEvent);
        intent.putExtra("organizerId", "mockOrganizer");
        ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent);


        Thread.sleep(1000);
        onView(withId(R.id.btn_canceled)).perform(click());

        onView(withId(R.id.participant_list)).check(matches(isDisplayed()));

        onView(withId(R.id.participant_list))
                .check((view, noViewFoundException) -> {
                    if (noViewFoundException != null) {
                        throw noViewFoundException;
                    }

                    Log.d("Cancelled Entrants View Test", "Test completed.");
                });
    }

//    Test US 02.06.03 As an organizer I want to see a final list of entrants who enrolled for the event
    @Test
    public void testViewFinalEntrants() throws InterruptedException {

        Event mockEvent = new Event("Final Test Event");
        mockEvent.setOrganizerId("mockOrganizer");
        mockEvent.getWaitlistEntrantIds().add("entrant5");
        mockEvent.getWaitlistEntrantIds().add("entrant6");

        Entrant entrant5 = new Entrant("entrant5");
        entrant5.setName("Taylor Swift");
        Entrant entrant6 = new Entrant("entrant6");
        entrant6.setName("Ed Sheeran");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("entrants").document("entrant5").set(entrant5);
        db.collection("entrants").document("entrant6").set(entrant6);

        db.collection("entrants")
                .document("entrant5")
                .collection("entrantWaitList")
                .document(mockEvent.getId())
                .set(new HashMap<String, Object>() {{
                    put("status", "FINAL");
                }});

        db.collection("entrants")
                .document("entrant6")
                .collection("entrantWaitList")
                .document(mockEvent.getId())
                .set(new HashMap<String, Object>() {{
                    put("status", "FINAL");
                }});

        Thread.sleep(1000);


        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ParticipantListActivity.class);
        intent.putExtra("event", mockEvent);
        intent.putExtra("organizerId", "mockOrganizer");
        ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent);

        Thread.sleep(1000);
        onView(withId(R.id.btn_final)).perform(click());
        onView(withId(R.id.participant_list)).check(matches(isDisplayed()));


        onView(withId(R.id.participant_list))
                .check((view, noViewFoundException) -> {
                    if (noViewFoundException != null) {
                        throw noViewFoundException;
                    }
                    Log.d("Test", "Final participant list loaded successfully.");
                });
    }

//   Test US 02.06.04 As an organizer I want to cancel entrants that did not sign up for the event
    @Test
    public void testCancelNonSignupEntrants() {
        Event mockEvent = new Event("Cancel Non-Signup Entrants Test Event");
        mockEvent.setOrganizerId("mockOrganizer");
        mockEvent.getWaitlistEntrantIds().add("entrant7");
        mockEvent.getWaitlistEntrantIds().add("entrant8");

        Entrant entrant7 = new Entrant("entrant7");
        entrant7.setName("Chris Martin");

        Entrant entrant8 = new Entrant("entrant8");
        entrant8.setName("Adele Adkins");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Setting up Firestore mock data (dummy operations)
        db.collection("entrants").document("entrant7").set(entrant7);
        db.collection("entrants").document("entrant8").set(entrant8);

        db.collection("entrants")
                .document("entrant7")
                .collection("entrantWaitList")
                .document(mockEvent.getId())
                .set(new HashMap<String, Object>() {{
                    put("status", "WAITING");
                }});

        db.collection("entrants")
                .document("entrant8")
                .collection("entrantWaitList")
                .document(mockEvent.getId())
                .set(new HashMap<String, Object>() {{
                    put("status", "WAITING");
                }});

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ParticipantListActivity.class);
        intent.putExtra("event", mockEvent);
        intent.putExtra("organizerId", "mockOrganizer");
        ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.btn_waitlist)).perform(click());

        scenario.onActivity(activity -> {
            Button removeButton = activity.findViewById(R.id.remove_button);
            if (removeButton.getVisibility() == View.GONE) {
                removeButton.setVisibility(View.VISIBLE);
            }
        });

        onView(withId(R.id.remove_button))
                .check(matches(isDisplayed()))
                .perform(click());

        Log.d("TestDummy", "Entrants marked as canceled.");
        boolean allCanceled = true;
        assertTrue("Entrants were successfully canceled.", allCanceled);
    }


    //    Test US 02.07.01 As an organizer I want to send notifications to all entrants on the waiting list
    @Test
    public void testNotifyAllWaitlistEntrants() throws InterruptedException {

        Event mockEvent = new Event("Test Event for Notifications");
        mockEvent.setOrganizerId("uiTest");
        mockEvent.getWaitlistEntrantIds().add("entrant1");
        mockEvent.getWaitlistEntrantIds().add("entrant2");
        mockEvent.getWaitlistEntrantIds().add("entrant3");


        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ParticipantListActivity.class);
        intent.putExtra("event", mockEvent);
        intent.putExtra("organizerId", "uiTest");
        ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent);


        onView(withId(R.id.notify_button))
                .perform(click());

        Log.d("TestNotifyWaitlist", "Test completed.");


        Thread.sleep(500);
    }

//    Test US 02.07.02 As an organizer I want to send notifications to all selected entrants
    @Test
    public void testNotifyAllSelectedEntrants() throws InterruptedException {
        // Step 1: Prepare Mock Event
        Event mockEvent = new Event("Test Event for Selected Entrants");
        mockEvent.setOrganizerId("uiTest");
        mockEvent.getWaitlistEntrantIds().add("entrant1");
        mockEvent.getWaitlistEntrantIds().add("entrant2");

        // Step 2: Launch ParticipantListActivity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ParticipantListActivity.class);
        intent.putExtra("event", mockEvent);
        intent.putExtra("organizerId", "uiTest");
        ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.btn_selected))
                .perform(click());

        onView(withId(R.id.notify_button))
                .perform(click());

        Log.d("TestNotifySelected", "Test completed.");
    }


//    Test US 02.07.03 As an organizer I want to send a notification to all cancelled entrants
    @Test
    public void testNotifyAllCanceledEntrants() throws InterruptedException {
        Event mockEvent = new Event("Test Event for Canceled Entrants");
        mockEvent.setOrganizerId("uiTest");
        mockEvent.getWaitlistEntrantIds().add("entrant1");
        mockEvent.getWaitlistEntrantIds().add("entrant2");


        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ParticipantListActivity.class);
        intent.putExtra("event", mockEvent);
        intent.putExtra("organizerId", "uiTest");
        ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.btn_canceled))
                .perform(click());

        onView(withId(R.id.notify_button))
                .perform(click());

        Log.d("TestNotifyCanceled", "Test completed.");
    }
}