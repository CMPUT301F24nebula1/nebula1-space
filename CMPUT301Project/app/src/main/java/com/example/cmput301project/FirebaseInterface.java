package com.example.cmput301project;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.example.cmput301project.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public interface FirebaseInterface {
    void listenToEntrantUpdates(String userId);
    void listenToOrganizerUpdates(String userId);

    void retrieveUser(String userId, OnUserRetrievedListener listener);

    interface OnUserRetrievedListener {
        void onEntrantRetrieved(Entrant entrant);
        void onOrganizerRetrieved(Organizer organizer);
        void onUserNotFound();
        void onError(Exception e);
    }
    void addUser(User user);
    void addEntrant(Entrant e);

    void findEventInAllOrganizers(String eventId, OnEventFoundListener listener);
    interface OnEventFoundListener {
        void onEventFound(Event event);
        void onEventNotFound();
        void onError(Exception e);
    }

    void updateEntrantInFirebase(Entrant entrant);
    void uploadImage(Uri imageUri, OnImageUploadListener listener);
    interface OnImageUploadListener {
        void onUploadSuccess(String imageUrl);
        void onUploadFailure(Exception e);
    }

    void updateOrganizerInFirebase(Organizer organizer);
//    void addEventToFirebase(Event event, Callback callback);
//    public interface Callback {
//        void onSuccess();
//        void onFailure(Exception e);
//    }
    void updateOrganizerEvents(String id, ArrayList<Event> events);
    void uploadBitmapToFirebase(Bitmap bitmap, OnSuccessListener<String> successListener, OnFailureListener failureListener);

    LiveData<Entrant> getEntrantLiveData();
    LiveData<Organizer> getOrganizerLiveData();
    FirebaseFirestore getDb();
    void setDb(FirebaseFirestore db);
}

