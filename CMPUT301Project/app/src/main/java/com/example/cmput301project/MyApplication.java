package com.example.cmput301project;

import android.app.Application;
import android.provider.Settings;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MyApplication extends Application {
    private String userId;
    private Entrant entrant;
    private Organizer organizer;

    private FirebaseFirestore db;
    private MutableLiveData<Entrant> entrantLiveData = new MutableLiveData<>();
    private MutableLiveData<Organizer> organizerLiveData = new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);  // Initialize Firebase
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
                entrantLiveData.setValue(entrant);  // Update LiveData with the new organizer
            }
        });
    }

    public void listenToOrganizerFirebaseUpdates(String userId) {
        DocumentReference docRef = getDb().collection("organizers").document(userId);

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
                                for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                                    Event event = eventDoc.toObject(Event.class);
                                    eventsList.add(event);
                                }

                                // Set the events list in the organizer object
                                organizer.setEvents(eventsList);

                                // Update LiveData with the new organizer object that includes updated events
                                organizerLiveData.setValue(organizer);

                                Log.d("Firestore", "Organizer and events successfully updated and loaded.");
                            }
                        });
            } else if (snapshot != null && !snapshot.exists()) {
                // When the organizer document is deleted, update accordingly
                Organizer organizer = new Organizer(userId);
                organizerLiveData.setValue(organizer);
            }
        });
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

    public LiveData<Organizer> getOrganizerLiveData() {
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
