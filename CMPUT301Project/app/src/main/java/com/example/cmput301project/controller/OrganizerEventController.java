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

    public void addEvent(Event event, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {

        Bitmap b = QRCodeGenerator.generateQRCode(event.getId());

        fb.uploadBitmapToFirebase(b, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                String hashedQRCode = QRCodeGenerator.hashQRCode(QRCodeGenerator.convertBitmapToByteArray(b));
                event.setHashedQRCode(hashedQRCode);
                event.setQrCode(s);
                organizer.create_event(event);
                successListener.onSuccess(null);
            }
        }, failureListener);
    }

    public void editEvent(Event event, String name, String description, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        if (name != null) {
            event.setName(name);
        }
        if (description != null) {
            event.setDescription(description);
        }

    }
}

