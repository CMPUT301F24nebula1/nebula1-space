package com.example.cmput301project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrganizerEventController {
    private Organizer organizer;
    private FirebaseFirestore db;
    private Event e;

    public OrganizerEventController(Organizer organizer, FirebaseFirestore db) {
        this.organizer = organizer;
        this.db = db;
    }

    public void addEvent(String name, String description, Uri imageUri, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {

        Event event = new Event();

        Bitmap b = QRCodeGenerator.generateQRCode(event.getId());

        uploadBitmapToFirebase(b, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                String hashedQRCode = QRCodeGenerator.hashQRCode(QRCodeGenerator.convertBitmapToByteArray(b));
                event.setHashedQRCode(hashedQRCode);
                event.setName(name);
                event.setQrCode(s);
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
        }, failureListener);
    }

    public void editEvent(Event event, String name, String description, Uri imageUri, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        if (name != null) {
            event.setName(name);
        }
        if (description != null) {
            event.setDescription(description);
        }
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

    public Event findEventInAllOrganizers(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference organizersRef = db.collection("organizers");

        // 获取 organizers 集合中的所有文档
        organizersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Organizer organizer = document.toObject(Organizer.class);
                    if (organizer != null && organizer.getEvents() != null) {
                        // 遍历 Organizer 的 events 列表，查找匹配的 eventId
                        for (Event event : organizer.getEvents()) {
                            if (event.getId().equals(eventId)) {
                                // 找到匹配的 event，可以在这里处理相关逻辑
                                Log.d("Firestore", "Found event with ID: " + eventId + " in organizer: " + organizer.getId());
                                e = event;
                                return;
                            }
                        }
                    }
                }
                Log.d("Firestore", "No matching event found with ID: " + eventId);
            } else {
                Log.w("Firestore", "Error getting documents", task.getException());
            }
        });
        return e;
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
                .addOnFailureListener(failureListener);
    }


    private void saveEventToFirestore(Event event, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        DocumentReference userDocRef = db.collection("organizers").document(organizer.getId());
        userDocRef.update("events", FieldValue.arrayUnion(event))
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

        CollectionReference eventsCollection = db.collection("organizers")
                .document(organizer.getId())
                .collection("events");
        eventsCollection.document(event.getId())
                .set(event)
                .addOnSuccessListener(eventVoid -> Log.d("Firestore", "Event successfully added: " + event.getId()))
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding event: " + event.getId(), e));
    }

    private void updateEventInFirebase(String organizerId, Event updatedEvent, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        // Get a reference to the specific event document in the "events" subcollection
        DocumentReference eventDocRef = db.collection("organizers")
                .document(organizerId)
                .collection("events")
                .document(updatedEvent.getId());

        // Update the event document directly in Firestore
        eventDocRef.set(updatedEvent)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Event successfully updated!");
                    if (successListener != null) {
                        successListener.onSuccess(aVoid);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error updating event", e);
                    if (failureListener != null) {
                        failureListener.onFailure(e);
                    }
                });
    }
}

