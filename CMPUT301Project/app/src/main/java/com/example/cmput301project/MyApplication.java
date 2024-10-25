package com.example.cmput301project;

import android.app.Application;
import android.provider.Settings;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyApplication extends Application {
    private String userId;
    private Organizer organizer;
    private FirebaseFirestore db;
    private MutableLiveData<Organizer> organizerLiveData = new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);  // Initialize Firebase
    }

    public void listenToFirebaseUpdates(String userId) {
        DocumentReference docRef = getDb().collection("users").document(userId);

        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("Firebase", "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Organizer organizer = snapshot.toObject(Organizer.class);
                organizerLiveData.setValue(organizer);  // Update LiveData with the new organizer
            }
        });
    }


    public Organizer getOrganizer() {
        if (organizer == null) {
            organizer = new Organizer(userId);
        }
        return organizer;
    }

    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

    public FirebaseFirestore getDb() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }

    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    public String getUserId() {
        if (userId == null) {
            //userId = getDeviceId(getApplicationContext());
            userId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LiveData<Organizer> getOrganizerLiveData() {
        return organizerLiveData;
    }

    public void setOrganizerLiveData(Organizer organizer) {
        this.organizer = organizer;
        this.organizerLiveData.setValue(organizer);
    }
}
