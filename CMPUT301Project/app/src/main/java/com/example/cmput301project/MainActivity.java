package com.example.cmput301project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cmput301project.databinding.ActivityMainBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private FirebaseFirestore db;
    private CollectionReference userRef;
    private String id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getDeviceId(this);

        ((MyApplication) this.getApplication()).setUserId(id);
        ((MyApplication) this.getApplication()).setDb(FirebaseFirestore.getInstance());
        db = ((MyApplication) this.getApplication()).getDb();
        retrieveUser(id);
        ((MyApplication) this.getApplication()).listenToOrganizerFirebaseUpdates(id);
        ((MyApplication) this.getApplication()).listenToEntrantFirebaseUpdates(id);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        Intent intent = getIntent();
        String navigateTo = intent.getStringExtra("navigateTo");
        String eventId = intent.getStringExtra("eventId");

        Log.d("MainActivity", "navigateTo: " + navigateTo + ", eventId: " + eventId);

        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        if ("entrantEventViewFragment".equals(navigateTo)) {
            Log.d("MainActivity", "Navigating to entrantEventView");
            findEventInAllOrganizers(eventId, navController);
        }
    }

    public void findEventInAllOrganizers(String eventId, NavController nc) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference organizersRef = db.collection("organizers");

        organizersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot organizerDoc : task.getResult()) {
                    String organizerId = organizerDoc.getId();

                    // Get the "events" subcollection for the current organizer
                    CollectionReference eventsRef = organizersRef.document(organizerId).collection("events");

                    eventsRef.document(eventId).get().addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful() && eventTask.getResult() != null && eventTask.getResult().exists()) {
                            Event event = eventTask.getResult().toObject(Event.class);

                            if (event != null) {
                                Log.d("Firestore", "Found event with ID: " + eventId + " in organizer: " + organizerId);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("e", event);
                                nc.navigate(R.id.action_EntrantHomepage_to_EntrantEventView, bundle);
                            }
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
                                        db.collection("organizers").document(userId).collection("events")
                                                .get()
                                                .addOnSuccessListener(eventsSnapshot -> {
                                                    ArrayList<Event> eventsList = new ArrayList<>();
                                                    for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                                                        Event event = eventDoc.toObject(Event.class);
                                                        eventsList.add(event);
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
            Log.e("Firebase", "Error retrieving organizer", e);
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