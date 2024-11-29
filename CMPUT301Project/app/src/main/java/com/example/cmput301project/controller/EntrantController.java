package com.example.cmput301project.controller;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.view.EntrantEventViewFragment;
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

    public interface SaveCallback {
        void onSaveSuccess();
        void onSaveFailure(Exception e);
    }

    public void saveEntrantToDatabase(Entrant entrant, Uri u, SaveCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("name", entrant.getName());
        entrantData.put("email", entrant.getEmail());
        entrantData.put("phone", entrant.getPhone());
        entrantData.put("profilePictureUrl", entrant.getProfilePictureUrl());
        entrantData.put("latitude", entrant.getLatitude());
        entrantData.put("longitude", entrant.getLongitude());

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
                                if (callback != null) {
                                    callback.onSaveSuccess();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("saveEntrantToDatabase", "Failed to update entrant data in Firebase", e);
                                if (callback != null) {
                                    callback.onSaveFailure(e);
                                }
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
                        callback.onSaveSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("saveEntrantToDatabase", "Failed to update entrant data in Firebase", e);
                        callback.onSaveFailure(e);
                    });
        }
        Log.d("save entrant profile", entrant.toString());


    }

    public void joinEventWaitingList(Event event, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (event.isRequiresGeolocation()) {
            // Fetch and update location
            locationHelper.fetchEntrantLocation(context, new locationHelper.LocationCallback() {
                @Override
                public void onLocationRetrieved(double latitude, double longitude) {
                    // Update lat and long in Firebase
                    updateEntrantLocation(entrant, latitude, longitude, db);
                    // Add entrant to the waitlist
                    addEntrantToWaitlist(event, entrant, db);
                }

                @Override
                public void onLocationError(String errorMessage) {
                    Log.e("joinEventWaitingList", "Error fetching location: " + errorMessage);
                    // Even if location fails, add the entrant to the waitlist
                    addEntrantToWaitlist(event, entrant, db);
                }
            });
        } else {
            // Directly add entrant to waitlist if no geolocation is required
            addEntrantToWaitlist(event, entrant, db);
        }
    }

    private void addEntrantToWaitlist(Event event, Entrant entrant, FirebaseFirestore db) {
        Map<String, Object> waitlistData = new HashMap<>();

        db.collection("entrants")
                .document(entrant.getId())  // Assuming entrant has a unique ID
                .collection("entrantWaitList")
                .document(event.getId())
                .set(waitlistData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("addEntrantToWaitlist", "Entrant successfully added to event waitlist.");
                })
                .addOnFailureListener(e -> {
                    Log.e("addEntrantToWaitlist", "Failed to add entrant to event waitlist: " + e.getMessage(), e);
                });
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
    public void updateEntrantLocation(Entrant entrant, double latitude, double longitude, FirebaseFirestore db) {
        entrant.setLocation(latitude, longitude);
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);

        db.collection("entrants")
                .document(entrant.getId())
                .update(locationData)
                .addOnSuccessListener(aVoid -> Log.d("updateLocation", "Entrant location updated successfully."))
                .addOnFailureListener(e -> Log.e("updateLocation", "Error updating location: " + e.getMessage(), e));
    }

}
