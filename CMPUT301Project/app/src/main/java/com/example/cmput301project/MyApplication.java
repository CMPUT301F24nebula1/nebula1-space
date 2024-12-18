package com.example.cmput301project;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cmput301project.controller.UserController;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Notification;
import com.example.cmput301project.model.Organizer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Holding global variables
 * @author Xinjia Fan
 */
public class MyApplication extends Application {
    private String userId;
    private Entrant entrant;
    private Organizer organizer;
    private ArrayList<String> userRoles = new ArrayList<>();

    private FirebaseFirestore db;
    private MutableLiveData<Entrant> entrantLiveData = new MutableLiveData<>();
    private MutableLiveData<Organizer> organizerLiveData = new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);  // Initialize Firebase

        // Initialize Firestore and set the emulator only if it's not already initialized, only for testing
        if (db == null) {
            db = FirebaseFirestore.getInstance();

            if (isRunningTest()) {
                db.useEmulator("10.0.2.2", 8080);
                return;
            }
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build();
            db.setFirestoreSettings(settings);
        }
    }

    public void listenToEntrantFirebaseUpdates(String userId) {
        DocumentReference docRef = getDb().collection("entrants").document(userId);

        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("Firebase", "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Entrant entrant = snapshot.toObject(Entrant.class);
                retrieveEntrantWishlist(entrant);
                Log.d("wishlist track", "1");
//                setEntrantLiveData(entrant);
            } else {
                UserController.updateUserRole(userId, "entrant");
                addEntrant(new Entrant(userId));
                Entrant entrant = new Entrant(userId);
                this.setEntrantLiveData(entrant);
            }
        });
    }

    public void listenToOrganizerFirebaseUpdates(String userId) {
        DocumentReference docRef = getDb().collection("organizers").document(userId);

        Map<String, ListenerRegistration> subcollectionListeners = new HashMap<>();

        // Listen to the organizer document for updates
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("Firebase", "Listen failed.", e);
                return;
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

                                // Clear old subcollection listeners
                                for (ListenerRegistration listener : subcollectionListeners.values()) {
                                    listener.remove();
                                }
                                subcollectionListeners.clear();

                                for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                                    Object timestampValue = eventDoc.get("timestamp");
                                    // Print the type and value of the "timestamp" field
                                    if (timestampValue != null) {
                                        Log.d("Timestamp Type", "Type: " + timestampValue.getClass().getName());
                                        Log.d("Timestamp Value", "Value: " + timestampValue.toString());
                                    } else {
                                        Log.d("Timestamp", "Field 'timestamp' is null.");
                                    }
                                    Object name = eventDoc.get("name");
                                    Log.d("Timestamp name", name.toString());

                                    Event event = eventDoc.toObject(Event.class);
                                    eventsList.add(event);

                                    // Listen to the "userId" subcollection within each "events" document
                                    ListenerRegistration listener = eventDoc.getReference().collection("userId")
                                            .addSnapshotListener((userIdSnapshot, userIdError) -> {
                                                if (userIdError != null) {
                                                    Log.w("Firestore", "Error retrieving userId data", userIdError);
                                                    return;
                                                }

                                                if (userIdSnapshot != null) {
                                                    ArrayList<String> userIds = new ArrayList<>();
                                                    for (QueryDocumentSnapshot userIdDoc : userIdSnapshot) {
                                                        String user = userIdDoc.getId();
                                                        userIds.add(user);
                                                    }

                                                    // Update the specific event's userId list
                                                    event.setWaitlistEntrantIds(userIds);

                                                    // Update LiveData with the new organizer object, reflecting changes in events and userId data
                                                    organizer.setEvents(eventsList);
                                                    organizerLiveData.setValue(organizer);

                                                    Log.d("Firestore", "Organizer and userId data successfully updated and loaded for event " + eventDoc.getId());
                                                }
                                            });

                                    // Store the subcollection listener for cleanup later
                                    subcollectionListeners.put(eventDoc.getId(), listener);

                                }

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

    public void addEntrant(Entrant e) {
        db.collection("entrants").document(e.getId())
                .set(e)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User successfully added!");
                })
                .addOnFailureListener(f -> {
                    Log.w("Firestore", "Error adding user", f);
                });
    }

    private void retrieveEntrantWishlist(Entrant entrant) {
        CollectionReference waitlistRef = db.collection("entrants")
                .document(entrant.getId())
                .collection("entrantWaitList");
        ArrayList<String> wishlist = new ArrayList<>();

        waitlistRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e("Firestore Error", "Error listening to wishlist updates", error);
                return;
            }

            if (snapshots != null) {
                wishlist.clear(); // Clear previous data
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    String item = document.getString("eventId");
                    if (item != null) {
                        wishlist.add(item);
                    }
                }
                // Now `wishlist` contains the updated list of items
                Log.d("Wishlist app", "Wishlist items: " + wishlist);
                entrant.setWaitlistEventIds(wishlist);
                retrieveEntrantNotification(entrant, new NotificationCallback() {
                    @Override
                    public void onNotificationsRetrieved(ArrayList<Notification> notifications) {
                        setEntrantLiveData(entrant);
                        Log.d("retrieve entrant", "succeed");

                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("retrieve entrant", e.toString());
                    }
                });
            }
        });
    }

    public interface NotificationCallback {
        void onNotificationsRetrieved(ArrayList<Notification> notifications);
        void onError(Exception e);
    }

    private void retrieveEntrantNotification(Entrant entrant, NotificationCallback callback) {
        CollectionReference notificationRef = db.collection("entrants")
                .document(entrant.getId())
                .collection("notifications");
        ArrayList<Notification> notifications = new ArrayList<>();

        notificationRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e("Firestore Error", "Error listening to notification updates", error);
                return;
            }

            if (snapshots != null && !snapshots.isEmpty()) {
                notifications.clear();
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    Notification item = document.toObject(Notification.class);
                    if (item != null) {
                        item.setId(document.getId());
                        notifications.add(item);
                    }
                }
                Log.d("Notifications", "Notifications: " + notifications);
//                notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
                notifications.sort((n1, n2) -> {
                    Timestamp t1 = n1.getTimestamp();
                    Timestamp t2 = n2.getTimestamp();

                    if (t1 == null && t2 == null) return 0; // Both timestamps are null, treat as equal
                    if (t1 == null) return 1;               // Null timestamps are considered "less than"
                    if (t2 == null) return -1;

                    return t2.compareTo(t1);                // Compare non-null timestamps
                });

                entrant.setNotifications(notifications);
//                setEntrantLiveData(entrant);

                if (callback != null) {
                    callback.onNotificationsRetrieved(notifications);
                }
            }
        });
    }


    public void getUserRoles(OnRolesLoadedListener listener) {
        if (!userRoles.isEmpty()) {
            // If roles are already loaded, return them immediately
            listener.onRolesLoaded(userRoles);
            return;
        }

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            ArrayList<String> roles;
            Object roleObject = documentSnapshot.get("role");

            if (roleObject instanceof ArrayList) {
                roles = (ArrayList<String>) roleObject;
            } else {
                roles = new ArrayList<>(); // Initialize an empty list if no roles found
            }

            // Cache roles
            userRoles = roles;
            listener.onRolesLoaded(userRoles); // Notify listener with roles

        }).addOnFailureListener(e -> {
            Log.e("MyApplication", "Failed to retrieve user roles", e);
            listener.onRolesLoaded(new ArrayList<>()); // Return empty list on failure
        });
    }


    // Define the OnRolesLoadedListener interface within MyApplication
    public interface OnRolesLoadedListener {
        void onRolesLoaded(ArrayList<String> roles);
    }

    private boolean isRunningTest() {
        return "true".equals(System.getProperty("firebase.test"));
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
            // 10.0.2.2 is the special IP address to connect to the 'localhost' of
            // the host computer from an Android emulator.
//            db.useEmulator("10.0.2.2", 8080);
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

    public MutableLiveData<Organizer> getOrganizerLiveData() {
        return organizerLiveData;
    }

    public void setOrganizerLiveData(Organizer organizer) {
        this.organizer = organizer;
        this.organizerLiveData.setValue(organizer);
    }

    public Entrant getEntrant() {
        return entrant;
    }

    public void setEntrant(Entrant entrant) {
        this.entrant = entrant;
    }

    public MutableLiveData<Entrant> getEntrantLiveData() {
        return entrantLiveData;
    }

    public void setEntrantLiveData(Entrant entrant) {
        this.entrantLiveData.setValue(entrant);
        this.entrant = entrant;
        Log.d("wishlist track", "2");
    }
}
