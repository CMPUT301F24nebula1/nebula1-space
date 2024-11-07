package com.example.cmput301project;

import android.app.Application;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


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
                Log.d("pass entrant event", entrant.getWaitlistEventIds().toString());
                Log.i("livedata event", fb.getEntrantLiveData().getValue().getWaitlistEventIds().toString());
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
                Log.i("livedata event___", fb.getEntrantLiveData().getValue().getWaitlistEventIds().toString());
                Log.d("Entrant", "Entrant data gets updated locally");
                entrantLiveData.setValue(entrant);
            }
        });

            if (snapshot != null && snapshot.exists()) {
                Entrant entrant = snapshot.toObject(Entrant.class);
                retrieveEntrantWishlist(entrant);
                setEntrantLiveData(entrant); // Update LiveData with the new organizer
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


            if (snapshot != null && snapshot.exists()) {
                Organizer organizer = snapshot.toObject(Organizer.class);
                if (organizer == null) {
                    return;
                }

                // Listen for changes in the events subcollection
                docRef.collection("events")
                        .addSnapshotListener((eventsSnapshot, eventsError) -> {
                            if (eventsError != null) {
                                Log.w("Firestore", "Error retrieving events data", eventsError);
                                return;
                            }

                            if (eventsSnapshot != null) {
                                ArrayList<Event> eventsList = new ArrayList<>();
                                for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                                    Event event = eventDoc.toObject(Event.class);
                                    eventsList.add(event);
                                }

                                // Set the events list in the organizer object
                                organizer.setEvents(eventsList);

                                // Update LiveData with the new organizer object that includes updated events
                                organizerLiveData.setValue(organizer);

                                Log.d("Firestore", "Organizer and events successfully updated and loaded.");
                            }
                        });
            } else if (snapshot != null && !snapshot.exists()) {
                // When the organizer document is deleted, update accordingly
                Organizer organizer = new Organizer(userId);
                setOrganizerLiveData(organizer);
            }
        });
    }

    private void retrieveEntrantWishlist(Entrant entrant) {
        CollectionReference waitlistRef = db.collection("entrants").document(entrant.getId()).collection("entrantWaitList");
        ArrayList<String> wishlist = new ArrayList<>();

        waitlistRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                // Assuming each document has a field "item" that is a String
                                String item = document.getString("eventId");
                                if (item != null) {
                                    wishlist.add(item);
                                }
                            }
                            // Now `wishlist` contains all items
                            Log.d("Wishlist app", "Wishlist items: " + wishlist);
                            entrant.setWaitlistEventIds(wishlist);
                        } else {
                            Log.e("Firestore Error", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

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
