package com.example.cmput301project;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class EventController {
    private Organizer organizer;
    private FirebaseFirestore db;

    public EventController(Organizer organizer, FirebaseFirestore db) {
        this.organizer = organizer;
        this.db = db;
    }

    public void addEvent(String name, String description, Uri imageUri, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {

        Event event = new Event();

        Bitmap b = QRCodeGenerator.generateQRCode(event.getId());
        String hashedQRCode = QRCodeGenerator.hashQRCode(QRCodeGenerator.convertBitmapToByteArray(b));
        event.setHashedQRCode(hashedQRCode);

        event.setName(name);
        uploadBitmapToFirebase(b, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                event.setQrCode(s);
            }
        }, failureListener);

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

        // Upload QR Code

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
        DocumentReference userDocRef = db.collection("users").document(organizer.getId());
        userDocRef.update("events", FieldValue.arrayUnion(event))
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

}

