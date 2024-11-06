package com.example.cmput301project.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cmput301project.FirebaseInterface;
import com.example.cmput301project.MyApplication;
import com.example.cmput301project.model.Organizer;
import com.example.cmput301project.R;
import com.example.cmput301project.model.User;
import com.example.cmput301project.databinding.ActivityMainBinding;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

/**
 * MainActivity
 * @author Xinjia Fan
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private MyApplication app;
    private String id;

    // manages whether it's entrant homepage or organizer homepage
    private MaterialButtonToggleGroup toggleGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MyApplication) getApplication();
        id = getDeviceId(this);

        app.setUserId(id);

        app.getFb().retrieveUser(id, new FirebaseInterface.OnUserRetrievedListener() {
            @Override
            public void onEntrantRetrieved(Entrant entrant) {
                Log.d("MainActivity", "Entrant retrieved: " + entrant.getName());
                app.setEntrantLiveData(entrant);
            }

            @Override
            public void onOrganizerRetrieved(Organizer organizer) {
                Log.d("MainActivity", "Organizer retrieved with events.");
                app.setOrganizerLiveData(organizer);
            }

            @Override
            public void onUserNotFound() {
                Log.d("MainActivity", "User not found, creating new.");
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainActivity", "Error retrieving user: " + e.getMessage());
            }
        });
        app.listenToOrganizerFirebaseUpdates(id);
        app.listenToEntrantFirebaseUpdates(id);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);


        toggleGroup = findViewById(R.id.toggleGroup);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Set the initial fragment (e.g., EntrantFragment)
        if (savedInstanceState == null) {
            replaceFragment(new EntrantHomepageFragment());
            toggleGroup.check(R.id.btn_entrant); // Set Entrant as default selected
        }

        // Add listener to toggle group for switching fragments
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_organizer) {
                    Log.d("Navigation", "Navigating to Organizer Homepage");
                    // Navigate to OrganizerFragment
                    if (navController.getCurrentDestination().getId() != R.id.OrganizerHomepageFragment) {
                        navController.navigate(R.id.action_EntrantHomepage_to_OrganizerHomepage);
                    }
                } else if (checkedId == R.id.btn_entrant) {
                    Log.d("Navigation", "Navigating to Entrant Homepage");
                    // Navigate to EntrantFragment
                    if (navController.getCurrentDestination().getId() != R.id.EntrantHomepageFragment) {
                        navController.navigate(R.id.action_OrganizerHomepage_to_EntrantHomepage);
                    }
                }
            }
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // hide toolbar if it's homepage
            if (destination.getId() == R.id.EntrantHomepageFragment || destination.getId() == R.id.OrganizerHomepageFragment) {
                toolbar.setVisibility(View.GONE);
            } else {
                toolbar.setVisibility(View.VISIBLE);
            }

            if (destination.getId() == R.id.OrganizerHomepageFragment || destination.getId() == R.id.EntrantHomepageFragment) {
                toggleGroup.setVisibility(View.VISIBLE); // Show toggle group on Organizer and Entrant fragments
            } else {
                toggleGroup.setVisibility(View.GONE); // Hide toggle group on other fragments
            }
        });

        // after scanning qr code
        Intent intent = getIntent();
        String navigateTo = intent.getStringExtra("navigateTo");
        String eventId = intent.getStringExtra("eventId");

        Log.d("MainActivity", "navigateTo: " + navigateTo + ", eventId: " + eventId);

        if ("entrantEventViewFragment".equals(navigateTo)) {
            Log.d("MainActivity", "Navigating to entrantEventView");
            app.getFb().findEventInAllOrganizers(eventId, new FirebaseInterface.OnEventFoundListener() {
                @Override
                public void onEventFound(Event event) {
                    Log.d("MainActivity", "Event found: " + event.getName());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("e", event);
                    navController.navigate(R.id.action_EntrantHomepage_to_EntrantEventView, bundle);
                }

                @Override
                public void onEventNotFound() {
                    Log.d("MainActivity", "Event not found with ID: " + eventId);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("MainActivity", "Error finding event: " + e.getMessage());
                }
            });
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow();;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private String getDeviceId(Context context) {
        // Retrieve ANDROID_ID as the device ID
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}