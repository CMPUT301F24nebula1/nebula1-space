package com.example.cmput301project;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SharedViewModel extends ViewModel {
    private FirebaseFirestore db;
    private String userId;
    private Organizer organizer;


    public SharedViewModel(String userId) {
        db = FirebaseFirestore.getInstance();
        this.userId = userId;
        retrieveUser();
    }

    public void retrieveUser() {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d("Firestore", "Document exists!");
                organizer = documentSnapshot.toObject(Organizer.class);

                if (organizer != null) {
                    // Access userId and events
                    Log.d("Firestore", "User ID: " + organizer.getId());
                    ArrayList<Event> events = organizer.getEvents();
                    for (Event event : events) {
                        Log.d("Firestore", "Event Name: " + event.getName());
                    }
                }
            } else {
                Log.d("Firestore", "No such document!");
                organizer = new Organizer(userId);
                addUser(userId);
            }
        }).addOnFailureListener(e -> {
            Log.d("Firestore", "Error checking document", e);
        });
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public String getUserId() {
        return userId;
    }

    public Organizer getOrganizer() {
        return this.organizer;
    }

    public void addUser(String userId) {
        // Create a list of sample events
        ArrayList<Event> eventList = new ArrayList<>();

        // Create a new user with the userId and events
        Organizer o = new Organizer(userId);

        // Add the user to the "users" collection
        db.collection("users")
                .document(o.getId())  // Use userId as the document ID
                .set(o)
                .addOnSuccessListener(aVoid -> {
                    // Successfully added the user
                    Log.d("Firestore", "User successfully added!");
                })
                .addOnFailureListener(e -> {
                    // Failed to add the user
                    Log.w("Firestore", "Error adding user", e);
                });
    }
}

