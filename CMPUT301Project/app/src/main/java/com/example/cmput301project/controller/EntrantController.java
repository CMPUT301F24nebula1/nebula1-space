package com.example.cmput301project.controller;

import android.net.Uri;
import android.util.Log;

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

    public EntrantController(Entrant e) {
        this.entrant = e;
    }

    public void saveEntrantToDatabase(Entrant entrant, Uri u) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("name", entrant.getName());
        entrantData.put("email", entrant.getEmail());
        entrantData.put("phone", entrant.getPhone());

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
            entrantData.put("profilePictureUrl", null);
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("entrants")
                .document(entrant.getId())  // Assuming entrant has a unique ID
                .collection("entrantWaitList")
                .document(event.getId())
                .set(new HashMap<String, Object>() {{
                    put("eventId", event.getId());
                }})
                .addOnSuccessListener(aVoid -> {
                    Log.d("join event waiting list", "Entrant data updated successfully in Firebase");
                })
                .addOnFailureListener(e -> {
                    Log.e("join event waiting list", "Failed to update entrant data in Firebase", e);
                });
    }

    public void addToEventWaitingList(Event event) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("entrants")
                .document(entrant.getId())  // Assuming entrant has a unique ID
                .collection("entrantWaitList")
                .document(event.getId())  // Specify the document to delete by its event ID
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("leave event waiting list", "Entrant data deleted successfully from Firebase");
                })
                .addOnFailureListener(e -> {
                    Log.e("leave event waiting list", "Failed to delete entrant data from Firebase", e);
                });
    }

    public void removeFromEventWaitingList(Event event) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                                        // If the event with targetEventId exists, remove the user from the userId subcollection
                                        db.collection("organizers")
                                                .document(organizerId)
                                                .collection("events")
                                                .document(event.getId())
                                                .collection("userId")
                                                .document(entrant.getId())
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("removeUserFromEvent", "User ID removed successfully from the event: " + event.getId());
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("removeUserFromEvent", "Failed to remove user ID from the event: " + e.getMessage(), e);
                                                });
                                    } else {
                                        Log.d("removeUserFromEvent", "Event with ID " + event.getId() + " not found in organizer " + organizerId);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("removeUserFromEvent", "Failed to retrieve event: " + e.getMessage(), e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("removeUserFromEvent", "Failed to retrieve organizers: " + e.getMessage(), e);
                });
    }

    public void uploadImageToFirebase(Uri imageUri, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images/" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String downloadUrl = downloadUri.toString();
                            Log.d("uploadImageToFirebase", "Image URI: " + imageUri.toString());
                            successListener.onSuccess(downloadUrl);  // Pass the string URL
                        }))
                .addOnFailureListener(failureListener);

    }



}
