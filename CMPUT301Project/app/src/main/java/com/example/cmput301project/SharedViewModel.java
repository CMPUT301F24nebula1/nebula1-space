package com.example.cmput301project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;

public class SharedViewModel extends ViewModel {
    private FirebaseFirestore db;
    private String userId;
    private MutableLiveData<Organizer> organizerLiveData = new MutableLiveData<>();

    public SharedViewModel(String userId) {
        db = FirebaseFirestore.getInstance();
        this.userId = userId;
        retrieveUser();
    }

    public void retrieveUser() {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Organizer organizer = documentSnapshot.toObject(Organizer.class);
                if (organizer != null) {
                    organizerLiveData.setValue(organizer);  // Update LiveData when data is retrieved
                }
            } else {
                Organizer newOrganizer = new Organizer(userId);
                addUser(newOrganizer);
                organizerLiveData.setValue(newOrganizer);
            }
        }).addOnFailureListener(e -> {
            Log.d("Firestore", "Error retrieving document", e);
        });
    }

    public LiveData<Organizer> getOrganizerLiveData() {
        return organizerLiveData;
    }

    public void addUser(Organizer organizer) {
        db.collection("users").document(organizer.getId())
                .set(organizer)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User successfully added!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding user", e);
                });
    }
}
