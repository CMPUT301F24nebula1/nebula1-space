package com.example.cmput301project;

import android.app.Application;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Holding global variables
 * @author Xinjia Fan
 */
public class MyApplication extends Application {
    private String userId;
    private Entrant entrant;
    private Organizer organizer;
    private ArrayList<String> userRoles = new ArrayList<>();

    private FirebaseFirestore db;
    private MutableLiveData<Entrant> entrantLiveData = new MutableLiveData<>();
    private MutableLiveData<Organizer> organizerLiveData = new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);  // Initialize Firebase

        // Initialize Firestore and set the emulator only if it's not already initialized, only for testing
        if (db == null) {
            db = FirebaseFirestore.getInstance();
//            db.useEmulator("10.0.2.2", 8080);
//
//            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
//                    .setPersistenceEnabled(false)
//                    .build();
//            db.setFirestoreSettings(settings);
        }
    }

    public void listenToEntrantFirebaseUpdates(String userId) {
        DocumentReference docRef = getDb().collection("entrants").document(userId);

        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("Firebase", "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Entrant entrant = snapshot.toObject(Entrant.class);
                retrieveEntrantWishlist(entrant);
                setEntrantLiveData(entrant); // Update LiveData with the new organizer
            }
        });
    }

    public void listenToOrganizerFirebaseUpdates(String userId) {
        DocumentReference docRef = getDb().collection("organizers").document(userId);

        Map<String, ListenerRegistration> subcollectionListeners = new HashMap<>();

        // Listen to the organizer document for updates
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("Firebase", "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Organizer organizer = snapshot.toObject(Organizer.class);
                if (organizer == null) {
                    return;
                }

                // Listen for changes in the events subcollection
                docRef.collection("events")
                        .addSnapshotListener((eventsSnapshot, eventsError) -> {
                            if (eventsError != null) {
                                Log.w("Firestore", "Error retrieving events data", eventsError);
                                return;
                            }

                            if (eventsSnapshot != null) {
                                ArrayList<Event> eventsList = new ArrayList<>();

                                // Clear old subcollection listeners
                                for (ListenerRegistration listener : subcollectionListeners.values()) {
                                    listener.remove();
                                }
                                subcollectionListeners.clear();

                                for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                                    Event event = eventDoc.toObject(Event.class);
                                    eventsList.add(event);

                                    // Listen to the "userId" subcollection within each "events" document
                                    ListenerRegistration listener = eventDoc.getReference().collection("userId")
                                            .addSnapshotListener((userIdSnapshot, userIdError) -> {
                                                if (userIdError != null) {
                                                    Log.w("Firestore", "Error retrieving userId data", userIdError);
                                                    return;
                                                }

                                                if (userIdSnapshot != null) {
                                                    ArrayList<String> userIds = new ArrayList<>();
                                                    for (QueryDocumentSnapshot userIdDoc : userIdSnapshot) {
                                                        String user = userIdDoc.getId();
                                                        userIds.add(user);
                                                    }

                                                    // Update the specific event's userId list
                                                    event.setWaitlistEntrantIds(userIds);

                                                    // Update LiveData with the new organizer object, reflecting changes in events and userId data
                                                    organizer.setEvents(eventsList);
                                                    organizerLiveData.setValue(organizer);

                                                    Log.d("Firestore", "Organizer and userId data successfully updated and loaded for event " + eventDoc.getId());
                                                }
                                            });

                                    // Store the subcollection listener for cleanup later
                                    subcollectionListeners.put(eventDoc.getId(), listener);

                                }

//                                // Set the events list in the organizer object
//                                organizer.setEvents(eventsList);
//
//                                // Update LiveData with the new organizer object that includes updated events
//                                organizerLiveData.setValue(organizer);

                                Log.d("Firestore", "Organizer and events successfully updated and loaded.");
                            }
                        });
            } else if (snapshot != null && !snapshot.exists()) {
                // When the organizer document is deleted, update accordingly
                Organizer organizer = new Organizer(userId);
                setOrganizerLiveData(organizer);
            }
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
                                // Assuming each document has a field "item" that is a String
                                String item = document.getString("eventId");
                                if (item != null) {
                                    wishlist.add(item);
                                }
                            }
                            // Now `wishlist` contains all items
                            Log.d("Wishlist app", "Wishlist items: " + wishlist);
                            entrant.setWaitlistEventIds(wishlist);
                        } else {
                            Log.e("Firestore Error", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    public void getUserRoles(OnRolesLoadedListener listener) {
        if (!userRoles.isEmpty()) {
            // If roles are already loaded, return them immediately
            listener.onRolesLoaded(userRoles);
            return;
        }

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            ArrayList<String> roles;
            Object roleObject = documentSnapshot.get("role");

            if (roleObject instanceof ArrayList) {
                roles = (ArrayList<String>) roleObject;
            } else {
                roles = new ArrayList<>(); // Initialize an empty list if no roles found
            }

            // Cache roles
            userRoles = roles;
            listener.onRolesLoaded(userRoles); // Notify listener with roles

        }).addOnFailureListener(e -> {
            Log.e("MyApplication", "Failed to retrieve user roles", e);
            listener.onRolesLoaded(new ArrayList<>()); // Return empty list on failure
        });
    }


    // Define the OnRolesLoadedListener interface within MyApplication
    public interface OnRolesLoadedListener {
        void onRolesLoaded(ArrayList<String> roles);
    }

    public Organizer getOrganizer() {
        if (organizer == null) {
            organizer = new Organizer(userId);
        }
        return organizer;
    }

    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

    public FirebaseFirestore getDb() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
            // 10.0.2.2 is the special IP address to connect to the 'localhost' of
            // the host computer from an Android emulator.
//            db.useEmulator("10.0.2.2", 8080);
        }
        return db;
    }

    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    public String getUserId() {
        if (userId == null) {
            //userId = getDeviceId(getApplicationContext());
            userId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public MutableLiveData<Organizer> getOrganizerLiveData() {
        return organizerLiveData;
    }

    public void setOrganizerLiveData(Organizer organizer) {
        this.organizer = organizer;
        this.organizerLiveData.setValue(organizer);
    }

    public Entrant getEntrant() {
        return entrant;
    }

    public void setEntrant(Entrant entrant) {
        this.entrant = entrant;
    }

    public MutableLiveData<Entrant> getEntrantLiveData() {
        return entrantLiveData;
    }

    public void setEntrantLiveData(Entrant entrant) {
        this.entrantLiveData.setValue(entrant);
        this.entrant = entrant;
    }
}
