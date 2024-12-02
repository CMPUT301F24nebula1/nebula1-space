package com.example.cmput301project;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import com.example.cmput301project.controller.AdminQRController;
import com.example.cmput301project.controller.ImageRecyclerViewAdapter;
import com.example.cmput301project.controller.QRCodeGenerator;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.example.cmput301project.model.User;
import com.example.cmput301project.view.AdminAllEventsActivity;
import com.example.cmput301project.view.AdminAllProfilesActivity;
import com.example.cmput301project.view.AdminManageFacilitiesActivity;
import com.example.cmput301project.view.AdminManageImagesActivity;
import com.example.cmput301project.view.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.hamcrest.CoreMatchers.not;


@RunWith(AndroidJUnit4.class)
public class AdminTests {
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

//    US 03.05.01 As an administrator, I want to be able to browse profiles.
    @Test
    public void testAdminProfiles() throws InterruptedException {
        // Create an Intent with the required arguments
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest");

        // Launch the MainActivity with the Intent
        scenario =  ActivityScenario.launch(intent);

        Thread.sleep(1000);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.btn_admin))
                .perform(ViewActions.click());

        Thread.sleep(1000);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.manageProfilesButton))
                .perform(ViewActions.click());

        Thread.sleep(1000);
    }

//    US 03.04.01 As an administrator, I want to be able to browse events.
    @Test
    public void testAdminEvents() throws InterruptedException {
        // Create an Intent with the required arguments
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest");

        // Launch the MainActivity with the Intent
        scenario =  ActivityScenario.launch(intent);

        Thread.sleep(1000);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.btn_admin))
                .perform(ViewActions.click());

        Thread.sleep(1000);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.manageEventsButton))
                .perform(ViewActions.click());

        Thread.sleep(1000);
    }

//    US 03.06.01 As an administrator, I want to be able to browse images.
    @Test
    public void testAdminImages() throws InterruptedException {

        // Create an Intent with the required arguments
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest");

        // Launch the MainActivity with the Intent
        scenario =  ActivityScenario.launch(intent);

        Thread.sleep(1000);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.btn_admin))
                .perform(ViewActions.click());

        Thread.sleep(1000);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.manageImagesButton))
                .perform(ViewActions.click());

        Thread.sleep(1000);
    }

    @Test
    public void testData() throws InterruptedException {
        createTestUsersAndEntrants();
    }

//    US 03.02.01 As an administrator, I want to be able to remove profiles.
    @Test
    public void testDeleteProfile1() throws InterruptedException {
        // Create an Intent with the required arguments
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest");

        // Launch the MainActivity with the Intent
        scenario =  ActivityScenario.launch(intent);

        Thread.sleep(1000);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.btn_admin))
                .perform(ViewActions.click());

        Thread.sleep(1000);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.manageProfilesButton))
                .perform(ViewActions.click());

        Thread.sleep(1000);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        deleteEntrant("test6");

        Thread.sleep(5000);

    }


//    US 03.02.01 As an administrator, I want to be able to remove profiles.
    @Test
    public void testDeleteProfile2() throws InterruptedException {
        // Launch the activity
        ActivityScenario<AdminAllProfilesActivity> scenario = ActivityScenario.launch(AdminAllProfilesActivity.class);

        // Wait for RecyclerView to load (use IdlingResource for better synchronization in a real app)
        Thread.sleep(3000);

        scenario.onActivity(activity -> {
            // Find the RecyclerView
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);

            // Ensure RecyclerView is not null and has items
            assertNotNull("RecyclerView is null", recyclerView);
            assertTrue("RecyclerView is empty", recyclerView.getAdapter().getItemCount() > 0);

            // Get the ViewHolder at position 0
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
            assertNotNull("ViewHolder is null", viewHolder);

            // Find the delete button inside the ViewHolder
            View deleteButton = viewHolder.itemView.findViewById(R.id.deleteButton);
            assertNotNull("Delete button is null", deleteButton);

            // Perform click on the delete button
            deleteButton.performClick();
        });

        Thread.sleep(1000);

//        Espresso.onView(allOf(withText("Profile deleted successfully"), isDisplayed()))
//                .check(matches(isDisplayed()));

        // Use UiAutomator to verify the Toast message
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject toast = device.findObject(new UiSelector().textContains("Profile deleted successfully"));

//        assertTrue("Toast message not displayed", toast.exists());
    }

    @Test
    public void testNavigateFacility() throws InterruptedException {
        // Launch the AdminManageFacilitiesActivity
        ActivityScenario<AdminManageFacilitiesActivity> scenario = ActivityScenario.launch(AdminManageFacilitiesActivity.class);

        // Wait for facilities to load (use IdlingResource for better synchronization in a real app)
        Thread.sleep(3000);

        scenario.onActivity(activity -> {
            // Find the RecyclerView
            RecyclerView recyclerView = activity.findViewById(R.id.facilitiesRecyclerView);

            // Ensure RecyclerView is not null and has items
            assertNotNull("RecyclerView is null", recyclerView);
            assertTrue("RecyclerView is empty", recyclerView.getAdapter().getItemCount() > 0);

            // Get the ViewHolder at position 0
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
            assertNotNull("ViewHolder is null", viewHolder);
        });

        // Wait for Toast message (adjust as needed for your app's behavior)
        Thread.sleep(1000);

        // Verify the success Toast message (using UiAutomator)
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject toast = device.findObject(new UiSelector().textContains("Facility deleted successfully"));
//        assertTrue("Toast message not displayed", toast.exists());
    }

//    US 03.07.01 As an administrator I want to remove facilities that violate app policy
    @Test
    public void testDeleteFacility() throws InterruptedException {
        // Launch the AdminManageFacilitiesActivity
        ActivityScenario<AdminManageFacilitiesActivity> scenario = ActivityScenario.launch(AdminManageFacilitiesActivity.class);

        // Wait for facilities to load (use IdlingResource for better synchronization in a real app)
        Thread.sleep(3000);

        scenario.onActivity(activity -> {
            // Find the RecyclerView
            RecyclerView recyclerView = activity.findViewById(R.id.facilitiesRecyclerView);

            // Ensure RecyclerView is not null and has items
            assertNotNull("RecyclerView is null", recyclerView);
            assertTrue("RecyclerView is empty", recyclerView.getAdapter().getItemCount() > 0);

            // Get the ViewHolder at position 0
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
            assertNotNull("ViewHolder is null", viewHolder);

            // Find the delete button inside the ViewHolder
            View deleteButton = viewHolder.itemView.findViewById(R.id.deleteFacilityButton);
            try {
                assertNotNull("Delete button is null", deleteButton);

                // Perform click on the delete button
                deleteButton.performClick();
            } catch (AssertionError e) {
                return;
            }
        });

        // Wait for Toast message (adjust as needed for your app's behavior)
        Thread.sleep(1000);

        // Verify the success Toast message (using UiAutomator)
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject toast = device.findObject(new UiSelector().textContains("Facility deleted successfully"));
//        assertTrue("Toast message not displayed", toast.exists());
    }

    @Test
    public void testNavigateToEvent() throws InterruptedException {
        // Launch the AdminManageFacilitiesActivity
        ActivityScenario<AdminAllEventsActivity> scenario = ActivityScenario.launch(AdminAllEventsActivity.class);

        // Wait for facilities to load (use IdlingResource for better synchronization in a real app)
        Thread.sleep(3000);

        scenario.onActivity(activity -> {
            // Find the RecyclerView
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);

            // Ensure RecyclerView is not null and has items
            assertNotNull("RecyclerView is null", recyclerView);
            assertTrue("RecyclerView is empty", recyclerView.getAdapter().getItemCount() > 0);

            // Get the ViewHolder at position 0
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
            assertNotNull("ViewHolder is null", viewHolder);

            // Perform a click on the first item in the RecyclerView
            viewHolder.itemView.performClick();
        });

        // Wait for Toast message (adjust as needed for your app's behavior)
        Thread.sleep(1000);
    }

    @Test
    public void testDeleteEvent() throws InterruptedException {
        // Launch the AdminManageFacilitiesActivity
        ActivityScenario<AdminAllEventsActivity> scenario = ActivityScenario.launch(AdminAllEventsActivity.class);

        // Wait for facilities to load (use IdlingResource for better synchronization in a real app)
        Thread.sleep(3000);

        scenario.onActivity(activity -> {
            // Find the RecyclerView
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);

            // Ensure RecyclerView is not null and has items
            assertNotNull("RecyclerView is null", recyclerView);
            assertTrue("RecyclerView is empty", recyclerView.getAdapter().getItemCount() > 0);

            // Get the ViewHolder at position 0
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
            assertNotNull("ViewHolder is null", viewHolder);

            // Perform a click on the first item in the RecyclerView
            viewHolder.itemView.performClick();
        });

        // Wait for Toast message (adjust as needed for your app's behavior)
        Thread.sleep(1000);

        Espresso.onView(withId(R.id.deleteEventButton))
                .check(matches(isDisplayed())) // Ensure it exists and is visible
                .perform(click());

        Thread.sleep(1000);

        Espresso.onView(withText("Yes"))
                .inRoot(isDialog()) // Ensure it's within a dialog
                .check(matches(isDisplayed())) // Verify the button is displayed
                .perform(click()); // Click the "Yes" button

        Thread.sleep(2000);
    }

    // US 03.03.02 As an administrator, I want to be able to remove hashed QR code data
    @Test
    public void testNavigateToQR() throws InterruptedException {
        ActivityScenario<AdminQRController> scenario = ActivityScenario.launch(AdminQRController.class);

        // Wait for facilities to load (use IdlingResource for better synchronization in a real app)
        Thread.sleep(3000);

        scenario.onActivity(activity -> {
            // Find the RecyclerView
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);

            // Ensure RecyclerView is not null and has items
            assertNotNull("RecyclerView is null", recyclerView);
            assertTrue("RecyclerView is empty", recyclerView.getAdapter().getItemCount() > 0);

            // Get the ViewHolder at position 0
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
            assertNotNull("ViewHolder is null", viewHolder);

            // Perform a click on the first item in the RecyclerView
            viewHolder.itemView.performClick();
        });

        // Wait for Toast message (adjust as needed for your app's behavior)
        Thread.sleep(1000);
    }

    // US 03.03.02 As an administrator, I want to be able to remove hashed QR code data
    @Test
    public void testClickQR() throws InterruptedException {
        ActivityScenario<AdminQRController> scenario = ActivityScenario.launch(AdminQRController.class);

        // Wait for facilities to load (use IdlingResource for better synchronization in a real app)
        Thread.sleep(3000);

        scenario.onActivity(activity -> {
            // Find the RecyclerView
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);

            // Ensure RecyclerView is not null and has items
            assertNotNull("RecyclerView is null", recyclerView);
            assertTrue("RecyclerView is empty", recyclerView.getAdapter().getItemCount() > 0);

            // Get the ViewHolder at position 0
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
            assertNotNull("ViewHolder is null", viewHolder);

            // Find the QR code ImageView inside the ViewHolder
            ImageView qrCodeImageView = viewHolder.itemView.findViewById(R.id.qrCode);
            assertNotNull("QR code ImageView is null", qrCodeImageView);

            // Perform a click on the QR code ImageView
            qrCodeImageView.performClick();
        });

        // Wait for Toast message (adjust as needed for your app's behavior)
        Thread.sleep(1000);
    }

    @Test
    public void testDeleteQR() throws InterruptedException {
        ActivityScenario<AdminQRController> scenario = ActivityScenario.launch(AdminQRController.class);

        // Wait for facilities to load (use IdlingResource for better synchronization in a real app)
        Thread.sleep(3000);

        scenario.onActivity(activity -> {
            // Find the RecyclerView
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);

            // Ensure RecyclerView is not null and has items
            assertNotNull("RecyclerView is null", recyclerView);
            assertTrue("RecyclerView is empty", recyclerView.getAdapter().getItemCount() > 0);

            // Get the ViewHolder at position 0
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
            assertNotNull("ViewHolder is null", viewHolder);

            // Click on the item (to select it or show options)
            viewHolder.itemView.performClick();

        });

        // Wait for Toast message (adjust as needed for your app's behavior)
        Thread.sleep(1000);

        // Perform a click on the delete button (not inside RecyclerView)
        Espresso.onView(withId(R.id.deleteQRButton))
                .check(matches(isDisplayed()))  // Verify the button is displayed
                .perform(click());              // Click the delete button

        // Wait for confirmation or Toast message (adjust timing if necessary)
        Thread.sleep(1000);

        // Verify the Toast message (optional, if applicable)
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject toast = device.findObject(new UiSelector().textContains("Deleted successfully"));
    }

    @Test
    public void testClickImage() throws InterruptedException {

        ActivityScenario<AdminManageImagesActivity> scenario = ActivityScenario.launch(AdminManageImagesActivity.class);

        // Wait for facilities to load (use IdlingResource for better synchronization in a real app)
        Thread.sleep(3000);

        scenario.onActivity(activity -> {
            // Find the RecyclerView
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);

            // Ensure the RecyclerView is not null and has items
            assertNotNull("RecyclerView is null", recyclerView);
            assertTrue("RecyclerView is empty", recyclerView.getAdapter().getItemCount() > 0);

            // Find the ViewHolder at position 0 (or any desired position)
            int positionToClick = 0; // Change to the desired index
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToClick);
            assertNotNull("ViewHolder is null", viewHolder);

            // Perform a click on the itemView
            viewHolder.itemView.performClick();

            // Optional: Verify any changes or interactions triggered by the click
            // For example, checking if an item is selected
            ImageRecyclerViewAdapter adapter = (ImageRecyclerViewAdapter) recyclerView.getAdapter();
            assertNotNull("Adapter is null", adapter);
        });

        Thread.sleep(1000);
    }

    @Test
    public void testDeleteImage() throws InterruptedException {

        ActivityScenario<AdminManageImagesActivity> scenario = ActivityScenario.launch(AdminManageImagesActivity.class);

        // Wait for facilities to load (use IdlingResource for better synchronization in a real app)
        Thread.sleep(3000);

        scenario.onActivity(activity -> {
            // Find the RecyclerView
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);

            try {
                // Ensure the RecyclerView is not null
                assertNotNull("RecyclerView is null", recyclerView);

                // Ensure the RecyclerView has items
                assertNotNull("RecyclerView Adapter is null", recyclerView.getAdapter());
                assertTrue("RecyclerView is empty", recyclerView.getAdapter().getItemCount() > 0);
            } catch (AssertionError | NoMatchingViewException e) {
                Log.d("ui test", e.getMessage());
                return;
            }

            // Find the ViewHolder at position 0 (or any desired position)
            int positionToClick = 0; // Change to the desired index
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(positionToClick);
            assertNotNull("ViewHolder is null", viewHolder);

            // Perform a click on the itemView
            viewHolder.itemView.performClick();

            // Optional: Verify any changes or interactions triggered by the click
            // For example, checking if an item is selected
            ImageRecyclerViewAdapter adapter = (ImageRecyclerViewAdapter) recyclerView.getAdapter();
            assertNotNull("Adapter is null", adapter);
        });

        Thread.sleep(1000);

        Espresso.onView(withId(R.id.deletePoster))
                .perform(click());
    }

    @Test
    public void testNavigateDashboard() throws InterruptedException {
        // Create an Intent with the required arguments
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra("test_id", "uiTest");

        // Launch the MainActivity with the Intent
        scenario =  ActivityScenario.launch(intent);

        Thread.sleep(1000);

        // Click on the Organizer button in the toggle group
        Espresso.onView(ViewMatchers.withId(R.id.btn_admin))
                .perform(ViewActions.click());

        Thread.sleep(1000);
    }

    @Test
    public void testClearData() {
        clearFirebaseEmulatorData();
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

        uploadBitmapToFirebase(generateBitmap(), new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                testEntrants.get(0).setProfilePictureUrl(s);
                Log.d("profile url", "succeed");
                Log.d("profile url", s);
            }
        }, e -> {
            Log.d("profile url", "fails");
            Log.d("upload bitmap", "failure");
        });

        Thread.sleep(1000);

        int i = 0;
        // Add entrants inline
        for (Entrant entrant : testEntrants) {
            entrant.setName("test" + String.valueOf(i++));
            entrant.setEmail(entrant.getName() + "@email.com");
            if (entrant.getProfilePictureUrl() != null)
                Log.d("profile url entrant", entrant.getProfilePictureUrl());
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

        Organizer organizer1 = new Organizer("test6");
        organizer1.setName("UI Test Facility");
        organizer1.setEmail("facility@email.com");
        saveOrganizerToDatabase(organizer1);


        for (i = 0; i < 6; i++) {
            Event event = new Event("Test event" + String.valueOf(i));
            event.setStartDate("30/12/2025");
            event.setEndDate("30/12/2025");
            event.setLimit(0);
            if (i == 1) {
                event.setName("Geolocation enabled");
                event.setRequiresGeolocation(true);
            }
            addEvent("uiTest", event, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("ui test add event", "event added");
                    for (Entrant entrant : testEntrants) {
                        addEntrantToWaitlist(event, entrant, db);
                        addToEventWaitingList(event, entrant.getId());
                    }
                }
            }, e -> Log.d("ui test add event", "event added fails."));
        }

        // Wait for all operations to finish
        latch.await();

        Thread.sleep(5000);
        Log.d("Test", "All users and entrants have been created.");
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

//    private void addEvent(String id, Event event, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        CollectionReference eventsCollection = db.collection("organizers")
//                .document(id)
//                .collection("events");
//
//        eventsCollection.document(event.getId())
//                .set(event) // Save the event object first
//                .addOnSuccessListener(eventVoid -> {
//                    // Now update the 'timestamp' field with the server timestamp
//                    eventsCollection.document(event.getId())
//                            .update("timestamp", FieldValue.serverTimestamp()) // Update the timestamp field
//                            .addOnSuccessListener(aVoid -> {
//                                Log.d("Firestore", "Event and timestamp successfully added: " + event.getId());
//                                successListener.onSuccess(null); // Call the success listener
//                            })
//                            .addOnFailureListener(e -> {
//                                Log.e("Firestore", "Error updating timestamp for event: " + event.getId(), e);
//                                failureListener.onFailure(e); // Call the failure listener
//                            });
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("Firestore", "Error adding event: " + event.getId(), e);
//                    failureListener.onFailure(e); // Call the failure listener
//                });
//    }

    private void addEvent(String id, Event event, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("organizers")
                .document(id)
                .collection("events");

        Bitmap b = QRCodeGenerator.generateQRCode(event.getId());

        uploadBitmapToFirebase(b, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                String hashedQRCode = QRCodeGenerator.hashQRCode(QRCodeGenerator.convertBitmapToByteArray(b));
                event.setHashedQRCode(hashedQRCode);
                event.setQrCode(s);

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
        }, failureListener);

    }

    private void uploadBitmapToFirebase(Bitmap bitmap, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.useEmulator("10.0.2.2", 9199);
        StorageReference storageRef = storage.getReference();

        Log.d("firebase", "storage");

        StorageReference imageRef = storageRef.child("images/" + System.currentTimeMillis() + ".png");

        imageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String downloadUrl = downloadUri.toString();
                            Log.d("storage", "Success uploading bitmap");
                            successListener.onSuccess(downloadUrl);  // Return URL
                        }))
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error uploading bitmap", e);
                    if (failureListener != null) {
                        failureListener.onFailure(e);  // failureListener
                    }
                });
    }

    private void deleteEntrant(String entrantId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference entrantRef = db.collection("entrants").document(entrantId);

        // Step 1: Delete subcollections explicitly
        deleteSubcollection(entrantRef.collection("entrantWaitList"), db, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                deleteSubcollection(entrantRef.collection("notifications"), db, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Step 2: Delete the main document
                        entrantRef.delete().addOnSuccessListener(aVoid1 -> {
//                            deleteUserRole("entrant", entrantId, new AdminAllProfilesActivity.DeleteRoleCallback() {
//                                @Override
//                                public void onSuccess(String message) { }
//
//                                @Override
//                                public void onFailure(String error) { }
//                            });
                        }).addOnFailureListener(e -> { });
                    }
                }, e -> { });
            }
        }, e -> { });
    }

    private void deleteSubcollection(CollectionReference subcollection, FirebaseFirestore db, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        subcollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                WriteBatch batch = db.batch();
                for (DocumentSnapshot doc : task.getResult()) {
                    batch.delete(doc.getReference());
                }
                batch.commit().addOnCompleteListener(batchTask -> {
                    if (batchTask.isSuccessful()) {
                        onSuccess.onSuccess(null);
                    } else {
                        onFailure.onFailure(batchTask.getException());
                    }
                });
            } else {
                onFailure.onFailure(task.getException());
            }
        });
    }

    private Bitmap generateBitmap() {
        // Define the dimensions of the Bitmap
        int width = 500;
        int height = 500;

        // Create a Bitmap object
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Create a Canvas to draw on the Bitmap
        Canvas canvas = new Canvas(bitmap);

        // Fill the canvas with a background color
        canvas.drawColor(Color.LTGRAY);

        // Create a Paint object for drawing text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(50);
        textPaint.setAntiAlias(true); // Smooth edges for the text

        // Draw some text on the Bitmap
        canvas.drawText("Generated Image", 50, 100, textPaint);

        // Create a Paint object for drawing shapes
        Paint shapePaint = new Paint();
        shapePaint.setColor(Color.BLUE);
        shapePaint.setStyle(Paint.Style.FILL);

        // Draw a circle on the Bitmap
        canvas.drawCircle(250, 250, 100, shapePaint);

        // Draw a rectangle on the Bitmap
        shapePaint.setColor(Color.RED);
        canvas.drawRect(100, 350, 400, 450, shapePaint);

        // Return the generated Bitmap
        return bitmap;
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
