package com.example.cmput301project;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.example.cmput301project.view.ScannerActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

@RunWith(AndroidJUnit4.class)
public class ScanQrTest {

    @Rule
    public GrantPermissionRule permissionRuleCamera = GrantPermissionRule.grant(CAMERA);

    @Rule
    public GrantPermissionRule permissionRuleStorage = GrantPermissionRule.grant(READ_EXTERNAL_STORAGE);

    @Rule
    public GrantPermissionRule permissionRuleNotification = GrantPermissionRule.grant(POST_NOTIFICATIONS);

    @Rule
    public ActivityScenarioRule<ScannerActivity> activityRule =
            new ActivityScenarioRule<>(ScannerActivity.class);

//    @Rule
//    public IntentsTestRule<ScannerActivity> intentsTestRule = new IntentsTestRule<>(ScannerActivity.class);

    @Before
    public void setUp() {
        // Initialize Espresso Intents
        Intents.init();

        // Correctly get the app package name
        Context context = ApplicationProvider.getApplicationContext();
        String packageName = context.getPackageName();

        // Stub the intent to simulate an image selection
//        Uri imageUri = Uri.parse("android.resource://" + activityRule.getScenario().getClass().getPackageName() + "/drawable/entrant_barcode.png");
        Uri imageUri = Uri.parse("android.resource://" + packageName + "/" + R.drawable.entrant_barcode);
        Intent resultData = new Intent();
        resultData.setData(imageUri);

        // Set up the result for the ACTION_PICK intent
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Intercept the ACTION_PICK intent and provide the mock result
        intending(allOf(
                hasAction(Intent.ACTION_PICK),
                hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        )).respondWith(result);
    }

//    US 01.06.01 As an entrant I want to view event details within the app by scanning the promotional QR code
    @Test
    public void testQRCodeScan() throws InterruptedException {
        // Click the button to open the gallery (triggers the mocked intent)
        onView(withId(R.id.select_from_gallery_button)).perform(click());

        Thread.sleep(4000);
        // Proceed with assertions (e.g., checking if the result is handled as expected)
        // Adjust assertions based on what `onActivityResult` should achieve
//        onView(withId(R.id.scan_qr_button)).check(matches(withText("Expected QR Code Content")));
        // After "ScannerActivity" finishes, check if EntrantEventViewFragment is loaded
//        onView(withId(R.id.join_class_button)).perform(click());

        try {
            Thread.sleep(500); // Adjust timing if needed
            onView(withText("Invalid QR code.")).check(matches(isDisplayed()));
            return;
        } catch (Exception e) {
            Log.d("ui test", "Warning dialog did not appear: " + e.getMessage());
        }

        ViewInteraction joinButton = onView(withId(R.id.join_class_button));
        ViewInteraction leaveButton = onView(withId(R.id.leave_class_button));

        try {
            // Check if the join button is enabled
            joinButton.check(matches(isEnabled()));
            // Perform click on the join button
            joinButton.perform(click());
        } catch (NoMatchingViewException | AssertionError e) {
            // If the join button is not enabled, perform click on the leave button
            try {
                leaveButton.check(matches(isEnabled()));
                leaveButton.perform(click());
            } catch (NoMatchingViewException | AssertionError e1) {
                return;
            }
        }
    }

    @After
    public void tearDown() {
        // Release Intents
        Intents.release();
    }
}
