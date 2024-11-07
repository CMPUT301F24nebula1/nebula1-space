package com.example.cmput301project.controller;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.cmput301project.FirebaseInterface;
import com.example.cmput301project.MyApplication;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

/**
 * Acts as the controller for organizer to manipulate events.
 * @author Xinjia Fan
 */
public class OrganizerEventController {
    private Organizer organizer;
    //private FirebaseFirestore db;
    private FirebaseInterface fb;
    private Event event;

    public OrganizerEventController(Organizer organizer, FirebaseInterface fb) {
        this.organizer = organizer;
        this.fb = fb;
    }


    public void addEvent(Event event, Uri imageUri, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {


        Bitmap b = QRCodeGenerator.generateQRCode(event.getId());

        fb.uploadBitmapToFirebase(b, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                String hashedQRCode = QRCodeGenerator.hashQRCode(QRCodeGenerator.convertBitmapToByteArray(b));
                event.setHashedQRCode(hashedQRCode);

//                event.setName(name);
                event.setQrCode(s);
//                event.setStartDate(startDate);
//                event.setEndDate(endDate);
//                event.setDescription(description);

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
                successListener.onSuccess(null);
            }
        }, failureListener);
    }


    public void editEvent(Event event, Uri imageUri, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        if (imageUri != null) {
            uploadImageToFirebase(imageUri, new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String downloadUrl) {
                    event.setPosterUrl(downloadUrl);  // Set image URL in event
                    updateEventInFirebase(organizer.getId(), event, successListener, failureListener);
                }
            }, failureListener);
        } else {
            updateEventInFirebase(organizer.getId(), event, successListener, failureListener);
        }
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

    private void uploadBitmapToFirebase(Bitmap bitmap, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images/" + System.currentTimeMillis() + ".png");

        imageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String downloadUrl = downloadUri.toString();
                            successListener.onSuccess(downloadUrl);  // Return URL
                        }))
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error uploading bitmap", e);
                    if (failureListener != null) {
                        failureListener.onFailure(e);  // failureListener
                    }
                });
    }


    private void saveEventToFirestore(Event event, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        CollectionReference eventsCollection = db.collection("organizers")
                .document(organizer.getId())
                .collection("events");
        eventsCollection.document(event.getId())
                .set(event)
                .addOnSuccessListener(eventVoid -> {
                    Log.d("Firestore", "Event successfully added: " + event.getId());
                    successListener.onSuccess(null);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding event: " + event.getId(), e));
    }

    private void updateEventInFirebase(String organizerId, Event updatedEvent, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        // Get a reference to the specific event document in the "events" subcollection
        DocumentReference eventDocRef = db.collection("organizers")
                .document(organizerId)
                .collection("events")
                .document(updatedEvent.getId());

    }
}

