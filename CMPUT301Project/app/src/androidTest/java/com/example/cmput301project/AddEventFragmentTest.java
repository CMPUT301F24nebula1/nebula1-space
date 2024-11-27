package com.example.cmput301project;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;


import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.cmput301project.view.AddEventFragment;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
public class AddEventFragmentTest {

    private FragmentScenario<AddEventFragment> fragmentScenario;
    private TestMyApplication testApplication;

//    @Rule
//    public FragmentScenarioRule<AddEventFragment> fragmentRule =
//            new FragmentScenarioRule<>(AddEventFragment.class);

    @Before
    public void setUp() {
        // Get the Test Application and configure test data
        testApplication = (TestMyApplication) ApplicationProvider.getApplicationContext();
        testApplication.setMockData();  // Set up mock data in TestMyApplication

        // Launch the Fragment
        fragmentScenario = FragmentScenario.launchInContainer(AddEventFragment.class);
    }

    @Test
    public void test() throws InterruptedException {
        assert(1 == 1);
        Thread.sleep(2000);
    }

    @Test
    public void testEventCreationWithMockData() {
        NavController mockNavController = Mockito.mock(NavController.class);

        fragmentScenario.onFragment(fragment -> {
//            NavController navController = mock(NavController.class);
//            Navigation.setViewNavController(fragment.requireView(), navController);



            assertNotNull(fragment.getActivity());

            Navigation.setViewNavController(fragment.requireView(), mockNavController);


            // Fill in required fields
            fragment.binding.eventName.getEditText().setText("Mock Event");
            fragment.binding.startDateText.setText("01/01/2023");
            fragment.binding.endDateText.setText("01/02/2023");
            fragment.binding.lotteryCapacity.getEditText().setText("10");

            // Click the save button
            fragment.binding.saveEventButton.performClick();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Mockito.verify(mockNavController).navigate(R.id.action_AddEvent_to_EventDetail);

            // Verify navigation to the EventDetail screen
//            verify(navController).navigate(
//                    eq(AddEventFragmentDirections.actionAddEventToEventDetail(any(Event.class))),
//                    any(NavOptions.class)
//            );
        });
    }


//    @Test
//    public void testUploadImage() {
//        fragmentScenario.onFragment(fragment -> {
//            // Mock an image Uri for the image picker
//            Uri mockImageUri = Uri.parse("content://com.example.cmput301project/mock_image");
//            Intent resultData = new Intent();
//            resultData.setData(mockImageUri);
//
//            // Simulate the ActivityResult for selecting an image
//            fragment.imagePickerLauncher.launch(resultData);
////            fragment.imagePickerLauncher.onActivityResult(Activity.RESULT_OK, resultData);
//        });
//
//        // Verify the image was set in the ImageView
//        onView(withId(R.id.event_imageview))
//                .check(matches(isDisplayed()));
//    }
}