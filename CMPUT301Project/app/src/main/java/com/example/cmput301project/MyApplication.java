package com.example.cmput301project;

import android.app.Application;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;

import java.util.ArrayList;

/**
 * Holding global variables
 * @author Xinjia Fan
 */
public class MyApplication extends Application {
    private FirebaseInterface fb;

    private String userId;

    private MutableLiveData<Entrant> entrantLiveData = new MutableLiveData<>();
    private MutableLiveData<Organizer> organizerLiveData = new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();
        fb = new FirebaseServer(this);

        // observe local data
        entrantLiveData.observeForever(new Observer<Entrant>() {
            @Override
            public void onChanged(Entrant entrant) {
                Log.d("MyApplication", "Entrant data updated locally, pushing to Firebase");
                fb.updateEntrantInFirebase(entrant); // Push updates to Firebase
            }
        });

        organizerLiveData.observeForever(new Observer<Organizer>() {
            @Override
            public void onChanged(Organizer organizer) {
                Log.d("MyApplication", "Organizer data updated locally, pushing to Firebase");
                fb.updateOrganizerInFirebase(organizer);
            }
        });

        fb.getEntrantLiveData().observeForever(new Observer<Entrant>() {
            @Override
            public void onChanged(Entrant entrant) {
                Log.d("Entrant", "Entrant data gets updated locally");
                entrantLiveData.setValue(entrant);
            }
        });

        fb.getOrganizerLiveData().observeForever(new Observer<Organizer>() {
            @Override
            public void onChanged(Organizer organizer) {
                organizerLiveData.setValue(organizer);
            }
        });
    }

    public void uploadImageAndSetEntrant(Uri imageUri, Entrant entrant) {
        fb.uploadImage(imageUri, new FirebaseInterface.OnImageUploadListener() {
            @Override
            public void onUploadSuccess(String imageUrl) {
                // Set the profile picture URL in the entrant
                entrant.setProfilePictureUrl(imageUrl);

                // Save the updated entrant data with the new URL in Firebase
                entrantLiveData.setValue(entrant); // This will update Firebase and LiveData

                Log.d("MyApplication", "Image uploaded and Entrant updated with new URL.");
            }

            @Override
            public void onUploadFailure(Exception e) {
                Log.e("MyApplication", "Failed to upload image for entrant", e);
            }
        });
    }

    public void uploadImageAndSetEvent(Uri imageUri, Event event) {
        fb.uploadImage(imageUri, new FirebaseInterface.OnImageUploadListener() {

            @Override
            public void onUploadSuccess(String imageUrl) {
                event.setPosterUrl(imageUrl);
            }

            @Override
            public void onUploadFailure(Exception e) {
                Log.e("MyApplication", "Failed to upload image for event", e);
            }
        });
    }

    public void listenToEntrantFirebaseUpdates(String userId) {
        fb.listenToEntrantUpdates(userId);
    }

    public void listenToOrganizerFirebaseUpdates(String userId) {
        fb.listenToOrganizerUpdates(userId);
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
//        this.organizer = organizer;
        this.organizerLiveData.setValue(organizer);
    }

    public MutableLiveData<Entrant> getEntrantLiveData() {
        return entrantLiveData;
    }

    public void setEntrantLiveData(Entrant entrant) {
        this.entrantLiveData.setValue(entrant);
//        this.entrant = entrant;
    }

    public FirebaseInterface getFb() {
        return fb;
    }

    public void setFb(FirebaseInterface fb) {
        this.fb = fb;
    }
}
