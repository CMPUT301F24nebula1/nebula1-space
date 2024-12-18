package com.example.cmput301project.controller;

import android.util.Log;

import com.example.cmput301project.model.Admin;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.cmput301project.model.Admin;


import java.util.ArrayList;

/**
 * Acts as controller for user.
 * @author Xinjia Fan
 */
public class UserController {

    public static void updateUserRole(String userId, String roleToAdd) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                ArrayList<String> roles = (ArrayList<String>) documentSnapshot.get("role");
                if (roles != null && !roles.contains(roleToAdd)) {
                    roles.add(roleToAdd);
                    userRef.update("role", roles)
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "User role successfully updated!"))
                            .addOnFailureListener(e -> Log.w("Firestore", "Error updating user role", e));

                    addOrganizer(new Organizer(userId));
//                    addAdmin(new Admin(userId));

                    DocumentReference entrantRef = db.collection("entrants").document(userId);
                    entrantRef.get().addOnSuccessListener(documentSnapshot1 -> {
//                        ArrayList<String> roles1 = (ArrayList<String>) documentSnapshot.get("role");
//                        roles1.add("organizer");
                        entrantRef.update("role", roles)
                                .addOnSuccessListener(aVoid -> Log.e("Firestore", "Entrant role successfully updated!"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error updating entrant role", e));
                    }).addOnFailureListener(e -> Log.e("Firestore", "Error getting entrant document", e));

                } else {
                    Log.d("Firestore", "Role already exists or roles is null");
                }
            } else {
                Log.d("Firestore", "No such document");
            }
        }).addOnFailureListener(e -> Log.w("Firestore", "Error getting document", e));
    }

    public static void addOrganizer(Organizer organizer) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add the main organizer document
        db.collection("organizers").document(organizer.getId())
                .set(organizer)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Organizer successfully added!");

                    // Now add each event as a subdocument in the "events" subcollection
                    if (organizer.getEvents() != null && !organizer.getEvents().isEmpty()) {
                        CollectionReference eventsCollection = db.collection("organizers")
                                .document(organizer.getId())
                                .collection("events");

                        for (Event event : organizer.getEvents()) {
                            eventsCollection.document(event.getId())
                                    .set(event)
                                    .addOnSuccessListener(eventVoid -> Log.d("Firestore", "Event successfully added: " + event.getId()))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding event: " + event.getId(), e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding organizer", e));
    }

    public static void addAdmin(Admin admin) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Add the main admin document
        db.collection("admins").document(admin.getId())
                .set(admin)
                .addOnSuccessListener(aVoid -> {Log.d("Firestore", "Admin successfully added!");})
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding admin", e));
    }

}