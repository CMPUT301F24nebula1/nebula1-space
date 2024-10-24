package com.example.cmput301project;

import android.net.Uri;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AddEventController {
    private Organizer organizer;
    private FirebaseFirestore db;

    public AddEventController(Organizer organizer, FirebaseFirestore db) {
        this.organizer = organizer;
        this.db = db;
    }

    public void addEvent(String name, String description, Uri imageUri, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        Event event = new Event();
        event.setName(name);
        if (description != null)
            event.setDescription(description);

        // Upload image if present
        if (imageUri != null) {
            uploadImageToFirebase(imageUri, new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String downloadUrl) {
                    event.setPosterUrl(downloadUrl);  // Set image URL in event
                    saveEventToFirestore(event, successListener, failureListener);
                }
            }, failureListener);
        } else {
            saveEventToFirestore(event, successListener, failureListener);
        }
        organizer.create_event(event);
    }

    private void uploadImageToFirebase(Uri imageUri, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images/" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String downloadUrl = downloadUri.toString();
                            successListener.onSuccess(downloadUrl);  // Pass the string URL
                        }))
                .addOnFailureListener(failureListener);

    }

    private void saveEventToFirestore(Event event, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        DocumentReference userDocRef = db.collection("users").document(organizer.getId());
        userDocRef.update("events", FieldValue.arrayUnion(event))
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
}

