package com.example.cmput301project.controller;

import android.net.Uri;
import android.util.Log;

import com.example.cmput301project.FirebaseServer;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Entrant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Acts as the controller for an entrant.
 * @author Xinjia Fan
 */
public class EntrantController {
    private Entrant entrant;
    FirebaseServer fb;

    public EntrantController(Entrant e) {
        this.entrant = e;

    }

    public void saveEntrantToDatabase(Entrant entrant, Uri u) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("name", entrant.getName());
        entrantData.put("email", entrant.getEmail());
        entrantData.put("phone", entrant.getPhone());
        entrantData.put("profilePictureUrl", entrant.getProfilePictureUrl());

        if (u != null) {
            uploadImageToFirebase(u, new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String downloadUrl) {
                    Log.e("upload profile image", "success");
                    entrant.setProfilePictureUrl(downloadUrl);
                    entrantData.put("profilePictureUrl", downloadUrl);
                    db.collection("entrants")
                            .document(entrant.getId())
                            .update(entrantData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("saveEntrantToDatabase", "Entrant data updated successfully in Firebase");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("saveEntrantToDatabase", "Failed to update entrant data in Firebase", e);
                            });
                }
            }, e -> {
                Log.e("upload profile image", "failure uploading profile image");
            });
        }
        else {
//            entrantData.put("profilePictureUrl", null);
            db.collection("entrants")
                    .document(entrant.getId())
                    .update(entrantData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("saveEntrantToDatabase", "Entrant data updated successfully in Firebase");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("saveEntrantToDatabase", "Failed to update entrant data in Firebase", e);
                    });
        }
        Log.d("save entrant profile", entrant.toString());
    }

    public void joinEventWaitingList(Event event) {
        entrant.join_event(event);
        //fb.joinEventWaitingList(event, entrant.getId());
    }

    public void addToEventWaitingList(Event event) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        event.getWaitlistEntrantIds().add(entrant.getId());

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
                                                .document(entrant.getId())
                                                .set(new HashMap<String, Object>() {{
                                                    put("userId", entrant.getId());
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

    public void leaveEventWaitingList(Event event) {
        entrant.leave_event(event);
        //fb.leaveEventWaitingList(event, entrant.getId());
    }

    public void removeFromEventWaitingList(Event event) {
        event.remove_entrant(entrant);
        fb.removeFromEventWaitingList(event, entrant.getId());
    }

}
