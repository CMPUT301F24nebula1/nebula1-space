package com.example.cmput301project.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cmput301project.MyApplication;
import com.example.cmput301project.controller.UserController;
import com.example.cmput301project.model.Organizer;
import com.example.cmput301project.R;
import com.example.cmput301project.model.User;
import com.example.cmput301project.databinding.ActivityMainBinding;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

    private FirebaseFirestore db;
    private CollectionReference userRef;
    private String id;

    // manages whether it's entrant homepage or organizer homepage
    private MaterialButtonToggleGroup toggleGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getDeviceId(this);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        ((MyApplication) this.getApplication()).setUserId(id);
        ((MyApplication) this.getApplication()).setDb(FirebaseFirestore.getInstance());
        db = ((MyApplication) this.getApplication()).getDb();
        retrieveUser(id);
        ((MyApplication) this.getApplication()).listenToOrganizerFirebaseUpdates(id);
        ((MyApplication) this.getApplication()).listenToEntrantFirebaseUpdates(id);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        setSupportActionBar(binding.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));

        Intent intent = getIntent();
        String navigateTo = intent.getStringExtra("navigateTo");
        String eventId = intent.getStringExtra("eventId");

        Log.d("MainActivity", "navigateTo: " + navigateTo + ", eventId: " + eventId);

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
                    UserController.updateUserRole(id, "organizer");
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

        if ("entrantEventViewFragment".equals(navigateTo)) {
            Log.d("MainActivity", "Navigating to entrantEventView");
            findEventInAllOrganizers(eventId, navController);
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow();;
    }

    public void findEventInAllOrganizers(String eventId, NavController nc) {
        Log.d("wishlist before nav1", ((MyApplication) this.getApplication()).getEntrantLiveData().getValue().getWaitlistEventIds().toString());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference organizersRef = db.collection("organizers");
        Log.d("wishlist before nav2", ((MyApplication) this.getApplication()).getEntrantLiveData().getValue().getWaitlistEventIds().toString());
        organizersRef.get().addOnCompleteListener(task -> {
            Log.d("wishlist before nav3", ((MyApplication) this.getApplication()).getEntrantLiveData().getValue().getWaitlistEventIds().toString());
            if (task.isSuccessful() && task.getResult() != null) {

                for (QueryDocumentSnapshot organizerDoc : task.getResult()) {
                    String organizerId = organizerDoc.getId();

                    // Get the "events" subcollection for the current organizer
                    CollectionReference eventsRef = organizersRef.document(organizerId).collection("events");

                    eventsRef.document(eventId).get().addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful() && eventTask.getResult() != null && eventTask.getResult().exists()) {
                            Event event = eventTask.getResult().toObject(Event.class);

                            ArrayList<String> userIdList = new ArrayList<>();

                            eventsRef.document(event.getId()).collection("userId").get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                                            // field "userId" that stores a string
                                            String userId = document.getString("userId");
                                            if (userId != null) {
                                                userIdList.add(userId);
                                            }
                                        }
                                        event.setWaitlistEntrantIds(userIdList);
                                        Log.d("wishlist of event1", event.getWaitlistEntrantIds().toString());
                                        if (event != null) {
                                            Log.d("Firestore", "Found event with ID: " + eventId + " in organizer: " + organizerId);
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("e", event);
                                            nc.navigate(R.id.action_EntrantHomepage_to_EntrantEventView, bundle);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle any errors here
                                        Log.e("FirestoreError", "Error retrieving user IDs", e);
                                    });
                            Log.d("wishlist of event2", event.getWaitlistEntrantIds().toString());


                        } else if (eventTask.isSuccessful() && (eventTask.getResult() == null || !eventTask.getResult().exists())) {
                            Log.d("Firestore", "No matching event found with ID: " + eventId + " in organizer: " + organizerId);
                        } else {
                            Log.w("Firestore", "Error getting event", eventTask.getException());
                        }
                    });
                }
            } else {
                Log.w("Firestore", "Error getting organizers", task.getException());
            }
        });
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

    private void retrieveUser(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                ArrayList<String> roles = (ArrayList<String>) documentSnapshot.get("role");
                if (roles != null && roles.contains("entrant")) {
                    // set up entrant part for this user

                    db.collection("entrants").document(userId).get()
                            .addOnSuccessListener(entrantSnapshot -> {
                                if (entrantSnapshot.exists()) {
                                    Entrant entrant = entrantSnapshot.toObject(Entrant.class);
                                    retrieveEntrantWishlist(entrant);
                                    ((MyApplication) getApplication()).setEntrantLiveData(entrant);
                                }
                                else {
                                    Entrant entrant = new Entrant(userId);
                                    ((MyApplication) getApplication()).setEntrantLiveData(entrant);
                                }
                            })
                            .addOnFailureListener(e -> Log.w("Firestore", "Error retrieving entrant data", e));

                }
                if (roles != null && roles.contains("organizer")) {
                    // set up organizer part for this user

                    db.collection("organizers").document(userId).get()
                            .addOnSuccessListener(organizerSnapshot -> {
                                if (organizerSnapshot.exists()) {
                                    Organizer organizer = organizerSnapshot.toObject(Organizer.class);

                                    if (organizer != null) {
                                        // Retrieve events from the subcollection
                                        CollectionReference eventsRef = db.collection("organizers").document(userId).collection("events");
                                        eventsRef.get()
                                                .addOnSuccessListener(eventsSnapshot -> {
                                                    ArrayList<Event> eventsList = new ArrayList<>();
                                                    for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                                                        Event event = eventDoc.toObject(Event.class);

                                                        eventsRef.document(event.getId()).get().addOnCompleteListener(eventTask -> {
                                                            if (eventTask.isSuccessful() && eventTask.getResult() != null && eventTask.getResult().exists()) {
//                                                                Event event = eventTask.getResult().toObject(Event.class);

                                                                ArrayList<String> wishlishIds = new ArrayList<>();

                                                                eventsRef.document(event.getId()).collection("userId").get()
                                                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                                                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                                                                // field "userId" that stores a string
                                                                                String entrantId = document.getString("userId");
                                                                                if (entrantId != null) {
                                                                                    wishlishIds.add(entrantId);
                                                                                }
                                                                            }
                                                                            event.setWaitlistEntrantIds(wishlishIds);
                                                                            if (event != null) {
                                                                                Log.d("Firestore", "Found event with ID: " + event.getId() + " in organizer: " + organizer.getId());
                                                                                eventsList.add(event);
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(e -> {
                                                                            // Handle any errors here
                                                                            Log.e("FirestoreError", "Error retrieving user IDs", e);
                                                                        });

                                                            } else if (eventTask.isSuccessful() && (eventTask.getResult() == null || !eventTask.getResult().exists())) {
                                                                Log.d("Firestore", "No matching event found with ID: " + event.getId() + " in organizer: " + organizer.getId());
                                                            } else {
                                                                Log.w("Firestore", "Error getting event", eventTask.getException());
                                                            }
                                                        });
                                                    }

                                                    // Set the events list in the organizer object
                                                    organizer.setEvents(eventsList);

                                                    // Update the global instance and LiveData
                                                    ((MyApplication) getApplication()).setOrganizer(organizer);
                                                    ((MyApplication) getApplication()).setOrganizerLiveData(organizer);

                                                    Log.d("Firestore", "Organizer and events successfully loaded.");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w("Firestore", "Error retrieving events data", e);
                                                });
                                    }
                                } else {
                                    Log.w("Firestore", "Organizer document does not exist.");
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Firestore", "Error retrieving organizer data", e);
                            });

                }

            } else {
                User u = new User(userId);
                addUser(u);
                addEntrant(new Entrant(userId));
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Error retrieving user", e);
        });
    }

    private void retrieveEntrantWishlist(Entrant entrant) {
        CollectionReference waitlistRef = db.collection("entrants").document(entrant.getId()).collection("entrantWaitList");
        ArrayList<String> wishlist = new ArrayList<>();

        waitlistRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String item = document.getString("eventId");
                                if (item != null) {
                                    wishlist.add(item);
                                }
                            }
                            // Now `wishlist` contains all items
                            Log.d("Wishlist main", "Wishlist items: " + wishlist);
                            entrant.setWaitlistEventIds(wishlist);
                        } else {
                            Log.e("Firestore Error", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void addUser(User user) {
        db.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User successfully added!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding user", e);
                });
    }

    public void addEntrant(Entrant e) {
        db.collection("entrants").document(e.getId())
                .set(e)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User successfully added!");
                })
                .addOnFailureListener(f -> {
                    Log.w("Firestore", "Error adding user", f);
                });
    }

}