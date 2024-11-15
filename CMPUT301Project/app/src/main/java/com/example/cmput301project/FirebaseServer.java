//package com.example.cmput301project;
//
//import android.app.Application;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.util.Log;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//
//import com.example.cmput301project.model.Entrant;
//import com.example.cmput301project.model.Event;
//import com.example.cmput301project.model.Organizer;
//import com.example.cmput301project.model.User;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//
//import java.io.ByteArrayOutputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class FirebaseServer implements FirebaseInterface {
//    private FirebaseFirestore db;
//    private MutableLiveData<Entrant> entrantLiveData = new MutableLiveData<>();
//    private MutableLiveData<Organizer> organizerLiveData = new MutableLiveData<>();
//
//    private Entrant firebaseEntrant;
//
//    private boolean isUpdatingFromLocal = false;
//
//    public FirebaseServer() {
//        db = FirebaseFirestore.getInstance();
//    }
//
//    public FirebaseServer(Application application) {
//        FirebaseApp.initializeApp(application);
//        db = FirebaseFirestore.getInstance();
//    }
//
//    @Override
//    public void listenToEntrantUpdates(String userId) {
//        DocumentReference docRef = db.collection("entrants").document(userId);
//        docRef.addSnapshotListener((snapshot, e) -> {
//            if (e != null) {
//                Log.w("Firebase", "Listen failed.", e);
//                return;
//            }
//            if (snapshot != null && snapshot.exists()) {
//                firebaseEntrant = snapshot.toObject(Entrant.class);
//                if (firebaseEntrant == null) {
//                    firebaseEntrant = new Entrant(userId);
//                }
//
//                db.collection("entrants").document(userId).collection("wishlistEventIds")
//                        .get()
//                        .addOnSuccessListener(eventsSnapshot -> {
//                            ArrayList<String> eventIds = new ArrayList<>();
//                            for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
//                                String id = eventDoc.getId(); // Assuming each document ID in subcollection is the event ID
//                                eventIds.add(id);
//                            }
//                            // Set the retrieved wishlist event IDs
//                            firebaseEntrant.setWaitlistEventIds(eventIds);
//
//                            // Update LiveData with the complete Entrant object, including wishlistEventIds
//                            if (!firebaseEntrant.equals(entrantLiveData.getValue()) && !isUpdatingFromLocal) {
//                                //isUpdatingFromLocal = false;
//                                Log.d("firebaseEntrant event...", firebaseEntrant.getWaitlistEventIds().toString());
//                                Log.i("livedata event", entrantLiveData.getValue().getWaitlistEventIds().toString());
//                                entrantLiveData.setValue(firebaseEntrant);
//                                isUpdatingFromLocal = true;
//                            }
//
//                            Log.d("Entrant", "data retrieved and updated with wishlistEventIds");
//                        })
//                        .addOnFailureListener(e1 -> Log.e("Entrant update", "Failed to retrieve wishlistEventIds", e1));
//            }
//        });
//    }
//
//    // observe firebase changes
//    @Override
//    public void listenToOrganizerUpdates(String userId) {
//        DocumentReference docRef = db.collection("organizers").document(userId);
//        docRef.addSnapshotListener((snapshot, e) -> {
//            if (e != null) {
//                Log.w("Firebase", "Listen failed.", e);
//                return;
//            }
//            if (snapshot != null && snapshot.exists()) {
//                Organizer organizer = snapshot.toObject(Organizer.class);
//                if (organizer != null) {
//                    docRef.collection("events")
//                            .addSnapshotListener((eventsSnapshot, eventsError) -> {
//                                if (eventsError != null) {
//                                    Log.w("Firestore", "Error retrieving events data", eventsError);
//                                    return;
//                                }
//                                if (eventsSnapshot != null) {
//                                    ArrayList<Event> eventsList = new ArrayList<>();
//                                    for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
//                                        Event event = eventDoc.toObject(Event.class);
//                                        eventsList.add(event);
//                                    }
//                                    organizer.setEvents(eventsList);
//                                    if (!organizer.equals(organizerLiveData.getValue()) && !isUpdatingFromLocal) {
//                                        //isUpdatingFromLocal = false;
//                                        organizerLiveData.setValue(organizer);
//                                        isUpdatingFromLocal = true;
//                                    }
//                                    Log.d("Firestore", "Organizer and events successfully updated and loaded.");
//                                }
//                            });
//                }
//            } else if (snapshot != null && !snapshot.exists()) {
//                Organizer organizer = new Organizer(userId);
//                isUpdatingFromLocal = false;
//                organizerLiveData.setValue(organizer);
//                isUpdatingFromLocal = true;
//            }
//        });
//    }
//
//    @Override
//    public void retrieveUser(String userId, OnUserRetrievedListener listener) {
//        DocumentReference docRef = db.collection("users").document(userId);
//        docRef.get().addOnSuccessListener(documentSnapshot -> {
//            if (documentSnapshot.exists()) {
//                ArrayList<String> roles = (ArrayList<String>) documentSnapshot.get("role");
//                if (roles != null && roles.contains("entrant")) {
//                    db.collection("entrants").document(userId).get()
//                            .addOnSuccessListener(entrantSnapshot -> {
//                                Entrant entrant;
//                                if (entrantSnapshot.exists()) {
//                                    entrant = entrantSnapshot.toObject(Entrant.class);
//                                    if (entrant != null) { // Double-check to prevent null
//                                        db.collection("entrants").document(userId).collection("wishlistEventIds")
//                                                .get()
//                                                .addOnSuccessListener(eventsSnapshot -> {
//                                                    ArrayList<String> eventIds = new ArrayList<>();
//                                                    for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
//                                                        String id = eventDoc.getId();
//                                                        eventIds.add(id);
//                                                    }
//                                                    entrant.setWaitlistEventIds(eventIds);
//                                                })
//                                                .addOnFailureListener(listener::onError);
//                                    }
//                                } else {
//                                    entrant = new Entrant(userId);
//                                }
//                                this.entrantLiveData.setValue(entrant);
//                                Log.d("Entrant", "data retrieved");
//                                listener.onEntrantRetrieved(entrant);
//                            })
//                            .addOnFailureListener(e -> listener.onError(e));
//                }
//                if (roles != null && roles.contains("organizer")) {
//                    db.collection("organizers").document(userId).get()
//                            .addOnSuccessListener(organizerSnapshot -> {
//                                Organizer organizer = organizerSnapshot.toObject(Organizer.class);
//                                if (organizer != null) {
//                                    db.collection("organizers").document(userId).collection("events")
//                                            .get()
//                                            .addOnSuccessListener(eventsSnapshot -> {
//                                                ArrayList<Event> eventsList = new ArrayList<>();
//                                                for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
//                                                    Event event = eventDoc.toObject(Event.class);
//                                                    eventsList.add(event);
//                                                }
//                                                organizer.setEvents(eventsList);
//                                                this.organizerLiveData.setValue(organizer);
//                                                listener.onOrganizerRetrieved(organizer);
//                                            })
//                                            .addOnFailureListener(listener::onError);
//                                } else {
//                                    listener.onUserNotFound();
//                                }
//                            })
//                            .addOnFailureListener(listener::onError);
//                }
//            } else {
//                listener.onUserNotFound();
//                addUser(new User(userId));
//                addEntrant(new Entrant(userId));
//            }
//        }).addOnFailureListener(listener::onError);
//    }
//
//    @Override
//    public void addUser(User user) {
//        db.collection("users").document(user.getId())
//                .set(user)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d("Firestore", "User successfully added!");
//                })
//                .addOnFailureListener(e -> {
//                    Log.w("Firestore", "Error adding user", e);
//                });
//    }
//
//    @Override
//    public void addEntrant(Entrant e) {
//        db.collection("entrants").document(e.getId())
//                .set(e)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d("Firestore", "User successfully added!");
//                })
//                .addOnFailureListener(f -> {
//                    Log.w("Firestore", "Error adding user", f);
//                });
//    }
//
//    @Override
//    public void findEventInAllOrganizers(String eventId, OnEventFoundListener listener) {
//        CollectionReference organizersRef = db.collection("organizers");
//
//        organizersRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful() && task.getResult() != null) {
//                for (QueryDocumentSnapshot organizerDoc : task.getResult()) {
//                    String organizerId = organizerDoc.getId();
//
//                    CollectionReference eventsRef = organizersRef.document(organizerId).collection("events");
//
//                    eventsRef.document(eventId).get().addOnCompleteListener(eventTask -> {
//                        if (eventTask.isSuccessful() && eventTask.getResult() != null && eventTask.getResult().exists()) {
//                            Event event = eventTask.getResult().toObject(Event.class);
//                            if (event != null) {
//                                listener.onEventFound(event);
//                            }
//                        } else if (eventTask.isSuccessful() && (eventTask.getResult() == null || !eventTask.getResult().exists())) {
//                            listener.onEventNotFound();
//                        } else {
//                            listener.onError(eventTask.getException());
//                        }
//                    });
//                }
//            } else {
//                listener.onError(task.getException());
//            }
//        });
//    }
//
//
//    @Override
//    public void updateEntrantInFirebase(Entrant entrant) {
//        if (isUpdatingFromLocal) {
//            isUpdatingFromLocal = false;
//            // This is an update originating from the application, not Firebase
//            DocumentReference docRef = FirebaseFirestore.getInstance().collection("entrants").document(entrant.getId());
//
//            Log.d("event before fb", entrant.getWaitlistEventIds().toString());
//            Log.i("livedata event", entrantLiveData.getValue().getWaitlistEventIds().toString());
//            // Update the main Entrant document (excluding waitlistEventIds)
//            docRef.set(entrant).addOnSuccessListener(aVoid -> {
//                Log.d("FirebaseServer", "Entrant data successfully updated in Firebase");
//
//
//                // Now update the waitlistEventIds subcollection
//                Log.d("event before update list", entrant.getWaitlistEventIds().toString());
//                updateWaitlistEventIds(entrant.getId(), entrant.getWaitlistEventIds());
//                Log.d("firebase entrant event", entrant.getWaitlistEventIds().toString());
//                isUpdatingFromLocal = true;
//
//            }).addOnFailureListener(e -> {
//                Log.w("FirebaseServer", "Error updating entrant data", e);
//                isUpdatingFromLocal = true;
//            });
//        }
//    }
//
//    @Override
//    public void uploadImage(Uri imageUri, OnImageUploadListener listener) {
//        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//        StorageReference imageRef = storageRef.child("images/" + System.currentTimeMillis() + ".jpg");
//
//        imageRef.putFile(imageUri)
//                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
//                        .addOnSuccessListener(downloadUri -> {
//                            String downloadUrl = downloadUri.toString();
//                            Log.d("FirebaseServer", "Image uploaded successfully. URL: " + downloadUrl);
//                            listener.onUploadSuccess(downloadUrl);  // Pass the URL back to MyApplication
//                        })
//                        .addOnFailureListener(e -> {
//                            Log.e("FirebaseServer", "Failed to get download URL", e);
//                            listener.onUploadFailure(e);
//                        }))
//                .addOnFailureListener(e -> {
//                    Log.e("FirebaseServer", "Failed to upload image", e);
//                    listener.onUploadFailure(e);
//                });
//    }
//
//    public void uploadBitmapToFirebase(Bitmap bitmap, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] data = baos.toByteArray();
//
//        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//        StorageReference imageRef = storageRef.child("images/" + System.currentTimeMillis() + ".png");
//
//        imageRef.putBytes(data)
//                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
//                        .addOnSuccessListener(downloadUri -> {
//                            String downloadUrl = downloadUri.toString();
//                            successListener.onSuccess(downloadUrl);  // Return URL
//                        }))
//                .addOnFailureListener(e -> {
//                    Log.e("Firebase", "Error uploading bitmap", e);
//                    if (failureListener != null) {
//                        failureListener.onFailure(e);  // failureListener
//                    }
//                });
//    }
//
//    @Override
//    public void updateOrganizerInFirebase(Organizer organizer) {
//        if (isUpdatingFromLocal) {
//            // Only update Firebase if there are real changes
////            if (!organizer.equals(organizerLiveData.getValue())) {
//            isUpdatingFromLocal = false;  // Prevent recursive update loop
//
//            DocumentReference docRef = FirebaseFirestore.getInstance()
//                    .collection("organizers")
//                    .document(organizer.getId());
//
//            docRef.set(organizer).addOnSuccessListener(aVoid -> {
//                Log.d("FirebaseServer", "Organizer data successfully updated in Firebase");
//
//                // Update the events subcollection if needed
//                updateOrganizerEvents(organizer.getId(), organizer.getEvents());
//
//                // After completing Firebase write, re-enable local updates
//                isUpdatingFromLocal = true;
//
//            }).addOnFailureListener(e -> {
//                Log.w("FirebaseServer", "Error updating organizer data", e);
//                isUpdatingFromLocal = true;  // Reset flag in case of failure
//            });
////            }
//        }
//    }
//
//
//    @Override
//    public void updateOrganizerEvents(String id, ArrayList<Event> events) {
//        CollectionReference eventsRef = FirebaseFirestore.getInstance()
//                .collection("organizers")
//                .document(id)
//                .collection("events");
//
//        eventsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
//            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//                doc.getReference().delete();
//            }
//
//            for (Event event : events) {
//                eventsRef.document(event.getId()).set(event);
//            }
//            Log.d("FirebaseServer", "Organizer events successfully updated in Firebase");
//        }).addOnFailureListener(e -> {
//            Log.e("FirebaseServer", "Organizer events successfully updated in Firebase");
//        });
//    }
//
//    @Override
//    public void addToEventWaitingList(Event event, String id) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        db.collection("organizers")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (DocumentSnapshot organizerDoc : queryDocumentSnapshots) {
//                        String organizerId = organizerDoc.getId();
//
//                        // Get the events subcollection for each organizer
//                        db.collection("organizers")
//                                .document(organizerId)
//                                .collection("events")
//                                .document(event.getId())
//                                .get()
//                                .addOnSuccessListener(eventDoc -> {
//                                    if (eventDoc.exists()) {
//                                        // If the event with targetEventId exists, add the new user to the userId subcollection
//                                        db.collection("organizers")
//                                                .document(organizerId)
//                                                .collection("events")
//                                                .document(event.getId())
//                                                .collection("userId")
//                                                .document(id)
//                                                .set(new HashMap<String, Object>() {{
//                                                    put("userId", id);
//                                                }}) // You can add any field-value pairs here if necessary
//                                                .addOnSuccessListener(aVoid -> {
//                                                    Log.d("addUserToEvent", "User ID added successfully to the event: " + event.getId());
//                                                })
//                                                .addOnFailureListener(e -> {
//                                                    Log.e("addUserToEvent", "Failed to add user ID to the event: " + e.getMessage(), e);
//                                                });
//                                    } else {
//                                        Log.d("addUserToEvent", "Event with ID " + event.getId() + " not found in organizer " + organizerId);
//                                    }
//                                })
//                                .addOnFailureListener(e -> {
//                                    Log.e("addUserToEvent", "Failed to retrieve event: " + e.getMessage(), e);
//                                });
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("addUserToEvent", "Failed to retrieve organizers: " + e.getMessage(), e);
//                });
//    }
//
//    @Override
//    public void removeFromEventWaitingList(Event event, String id) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        db.collection("organizers")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (DocumentSnapshot organizerDoc : queryDocumentSnapshots) {
//                        String organizerId = organizerDoc.getId();
//
//                        // Get the events subcollection for each organizer
//                        db.collection("organizers")
//                                .document(organizerId)
//                                .collection("events")
//                                .document(event.getId())
//                                .get()
//                                .addOnSuccessListener(eventDoc -> {
//                                    if (eventDoc.exists()) {
//                                        // If the event with targetEventId exists, remove the user from the userId subcollection
//                                        db.collection("organizers")
//                                                .document(organizerId)
//                                                .collection("events")
//                                                .document(event.getId())
//                                                .collection("userId")
//                                                .document(id)
//                                                .delete()
//                                                .addOnSuccessListener(aVoid -> {
//                                                    Log.d("removeUserFromEvent", "User ID removed successfully from the event: " + event.getId());
//                                                })
//                                                .addOnFailureListener(e -> {
//                                                    Log.e("removeUserFromEvent", "Failed to remove user ID from the event: " + e.getMessage(), e);
//                                                });
//                                    } else {
//                                        Log.d("removeUserFromEvent", "Event with ID " + event.getId() + " not found in organizer " + organizerId);
//                                    }
//                                })
//                                .addOnFailureListener(e -> {
//                                    Log.e("removeUserFromEvent", "Failed to retrieve event: " + e.getMessage(), e);
//                                });
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("removeUserFromEvent", "Failed to retrieve organizers: " + e.getMessage(), e);
//                });
//    }
//
////    public void joinEventWaitingList(Event event, String id) {
////        FirebaseFirestore db = FirebaseFirestore.getInstance();
////        db.collection("entrants")
////                .document(id)  // Assuming entrant has a unique ID
////                .collection("entrantWaitList")
////                .document(event.getId())
////                .set(new HashMap<String, Object>() {{
////                    put("eventId", event.getId());
////                }})
////                .addOnSuccessListener(aVoid -> {
////                    Log.d("join event waiting list", "Entrant data updated successfully in Firebase");
////                })
////                .addOnFailureListener(e -> {
////                    Log.e("join event waiting list", "Failed to update entrant data in Firebase", e);
////                });
////    }
//
//    @Override
//    public void leaveEventWaitingList(Event event, String id) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("entrants")
//                .document(id)  // Assuming entrant has a unique ID
//                .collection("entrantWaitList")
//                .document(event.getId())  // Specify the document to delete by its event ID
//                .delete()
//                .addOnSuccessListener(aVoid -> {
//                    Log.d("leave event waiting list", "Entrant data deleted successfully from Firebase");
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("leave event waiting list", "Failed to delete entrant data from Firebase", e);
//                });
//    }
//
//    private void updateWaitlistEventIds(String userId, List<String> waitlistEventIds) {
//        CollectionReference waitlistRef = FirebaseFirestore.getInstance()
//                .collection("entrants")
//                .document(userId)
//                .collection("wishlistEventIds");
//
//        // Clear the existing waitlistEventIds collection
//        waitlistRef.get().addOnSuccessListener(querySnapshot -> {
//            for (QueryDocumentSnapshot doc : querySnapshot) {
//                doc.getReference().delete();
//            }
//
//            // Add each event ID in waitlistEventIds to the subcollection
//            for (String eventId : waitlistEventIds) {
//                waitlistRef.document(eventId).set(new HashMap<>());
//            }
//
//            Log.d("FirebaseServer", "WaitlistEventIds successfully updated in Firebase");
//
//        }).addOnFailureListener(e -> {
//            Log.w("FirebaseServer", "Error updating waitlistEventIds in Firebase", e);
//        });
//    }
//
//    @Override
//    public LiveData<Entrant> getEntrantLiveData() {
//        return entrantLiveData;
//    }
//
//    @Override
//    public LiveData<Organizer> getOrganizerLiveData() {
//        return organizerLiveData;
//    }
//
//    public void setEntrantLiveData(Entrant entrant) {
//        this.entrantLiveData.setValue(entrant);
//    }
//
//    public void setOrganizerLiveData(Organizer organizer) {
//        this.organizerLiveData.setValue(organizer);
//    }
//
//    @Override
//    public FirebaseFirestore getDb() {
//        if (db == null) {
//            db = FirebaseFirestore.getInstance();
//        }
//        return db;
//    }
//
//    @Override
//    public void setDb(FirebaseFirestore db) {
//        this.db = db;
//    }
//}
//

package com.example.cmput301project;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.example.cmput301project.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseServer implements FirebaseInterface {
    private FirebaseFirestore db;
    private MutableLiveData<Entrant> entrantLiveData = new MutableLiveData<>();
    private MutableLiveData<Organizer> organizerLiveData = new MutableLiveData<>();
    private MutableLiveData<Entrant> entrantLiveData_fb = new MutableLiveData<>(); // only firebase can write, others can only read
    private MutableLiveData<Organizer> organizerLiveData_fb = new MutableLiveData<>();
    private Entrant firebaseEntrant;

    private boolean isUpdatingFromLocal = false;

    public FirebaseServer() {
        db = FirebaseFirestore.getInstance();
    }

    public FirebaseServer(Application application) {
        FirebaseApp.initializeApp(application);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void listenToEntrantUpdates(String userId) {
        DocumentReference docRef = db.collection("entrants").document(userId);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("Firebase", "Listen failed.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                firebaseEntrant = snapshot.toObject(Entrant.class);
                if (firebaseEntrant == null) {
                    firebaseEntrant = new Entrant(userId);
                }

                db.collection("entrants").document(userId).collection("wishlistEventIds")
                        .get()
                        .addOnSuccessListener(eventsSnapshot -> {
                            ArrayList<String> eventIds = new ArrayList<>();
                            for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                                String id = eventDoc.getId(); // Assuming each document ID in subcollection is the event ID
                                eventIds.add(id);
                            }
                            // Set the retrieved wishlist event IDs
                            firebaseEntrant.setWaitlistEventIds(eventIds);

                            // Update LiveData with the complete Entrant object, including wishlistEventIds
                            if (!firebaseEntrant.equals(entrantLiveData.getValue()) /*&& !isUpdatingFromLocal*/) {
                                //isUpdatingFromLocal = false;
//                                entrantLiveData.setValue(firebaseEntrant);
//                                isUpdatingFromLocal = true;
                                entrantLiveData_fb.setValue(firebaseEntrant);
                            }

                            Log.d("Entrant", "data retrieved and updated with wishlistEventIds");
                        })
                        .addOnFailureListener(e1 -> Log.e("Entrant update", "Failed to retrieve wishlistEventIds", e1));
            }
        });
    }

    // observe firebase changes
    @Override
    public void listenToOrganizerUpdates(String userId) {
        DocumentReference docRef = db.collection("organizers").document(userId);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("Firebase", "Listen failed.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Organizer organizer = snapshot.toObject(Organizer.class);
                if (organizer != null) {
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
                                    organizer.setEvents(eventsList);
                                    if (!organizer.equals(organizerLiveData.getValue()) /*&& !isUpdatingFromLocal*/) {
                                        //isUpdatingFromLocal = false;
//                                        organizerLiveData.setValue(organizer);
//                                        isUpdatingFromLocal = true;
                                        organizerLiveData_fb.setValue(organizer);
                                    }
                                    Log.d("Firestore", "Organizer and events successfully updated and loaded.");
                                }
                            });
                }
            } else if (snapshot != null && !snapshot.exists()) {
                Organizer organizer = new Organizer(userId);
//                isUpdatingFromLocal = false;
//                organizerLiveData.setValue(organizer);
//                isUpdatingFromLocal = true;
                organizerLiveData_fb.setValue(organizer);
            }
        });
    }

    @Override
    public void retrieveUser(String userId, OnUserRetrievedListener listener) {
        if (userId.equals("admin_user_id")) {  // Replace "admin_user_id" with the actual admin ID
            User admin = new User(userId);
            admin.addAdmin();  // Add the admin role using the method in User

            listener.onOrganizerRetrieved(new Organizer(userId)); // Treating admin as an organizer for demo
            return;
        }

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                ArrayList<String> roles = (ArrayList<String>) documentSnapshot.get("role");
                if (roles != null && roles.contains("entrant")) {
                    db.collection("entrants").document(userId).get()
                            .addOnSuccessListener(entrantSnapshot -> {
                                Entrant entrant;
                                if (entrantSnapshot.exists()) {
                                    entrant = entrantSnapshot.toObject(Entrant.class);
                                    if (entrant != null) { // Double-check to prevent null
                                        db.collection("entrants").document(userId).collection("wishlistEventIds")
                                                .get()
                                                .addOnSuccessListener(eventsSnapshot -> {
                                                    ArrayList<String> eventIds = new ArrayList<>();
                                                    for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                                                        String id = eventDoc.getId();
                                                        eventIds.add(id);
                                                    }
                                                    entrant.setWaitlistEventIds(eventIds);
                                                })
                                                .addOnFailureListener(listener::onError);
                                    }
                                } else {
                                    entrant = new Entrant(userId);
                                }
                                this.entrantLiveData.setValue(entrant);
                                Log.d("Entrant", "data retrieved");
                                listener.onEntrantRetrieved(entrant);
                            })
                            .addOnFailureListener(e -> listener.onError(e));
                }
                if (roles != null && roles.contains("organizer")) {
                    db.collection("organizers").document(userId).get()
                            .addOnSuccessListener(organizerSnapshot -> {
                                Organizer organizer = organizerSnapshot.toObject(Organizer.class);
                                if (organizer != null) {
                                    db.collection("organizers").document(userId).collection("events")
                                            .get()
                                            .addOnSuccessListener(eventsSnapshot -> {
                                                ArrayList<Event> eventsList = new ArrayList<>();
                                                for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                                                    Event event = eventDoc.toObject(Event.class);
                                                    eventsList.add(event);
                                                }
                                                organizer.setEvents(eventsList);
                                                this.organizerLiveData.setValue(organizer);
                                                listener.onOrganizerRetrieved(organizer);
                                            })
                                            .addOnFailureListener(listener::onError);
                                } else {
                                    listener.onUserNotFound();
                                }
                            })
                            .addOnFailureListener(listener::onError);
                }
            } else {
                listener.onUserNotFound();
                addUser(new User(userId));
                addEntrant(new Entrant(userId));
            }
        }).addOnFailureListener(listener::onError);
    }

    @Override
    public void addUser(User user) {
        db.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User successfully added!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding user", e);
                });
    }

    @Override
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

    @Override
    public void findEventInAllOrganizers(String eventId, OnEventFoundListener listener) {
        CollectionReference organizersRef = db.collection("organizers");

        organizersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot organizerDoc : task.getResult()) {
                    String organizerId = organizerDoc.getId();

                    CollectionReference eventsRef = organizersRef.document(organizerId).collection("events");

                    eventsRef.document(eventId).get().addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful() && eventTask.getResult() != null && eventTask.getResult().exists()) {
                            Event event = eventTask.getResult().toObject(Event.class);
                            if (event != null) {
                                listener.onEventFound(event);
                            }
                        } else if (eventTask.isSuccessful() && (eventTask.getResult() == null || !eventTask.getResult().exists())) {
                            listener.onEventNotFound();
                        } else {
                            listener.onError(eventTask.getException());
                        }
                    });
                }
            } else {
                listener.onError(task.getException());
            }
        });
    }


    @Override
    public void updateEntrantInFirebase(Entrant entrant) {
//        if (isUpdatingFromLocal) {
//            isUpdatingFromLocal = false;
        // This is an update originating from the application, not Firebase
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("entrants").document(entrant.getId());

        // Update the main Entrant document (excluding waitlistEventIds)
        docRef.set(entrant).addOnSuccessListener(aVoid -> {
            Log.d("FirebaseServer", "Entrant data successfully updated in Firebase");

            // Now update the waitlistEventIds subcollection
            updateWaitlistEventIds(entrant.getId(), entrant.getWaitlistEventIds());
            isUpdatingFromLocal = true;

        }).addOnFailureListener(e -> {
            Log.w("FirebaseServer", "Error updating entrant data", e);
            isUpdatingFromLocal = true;
        });
//        }
    }

    @Override
    public void uploadImage(Uri imageUri, OnImageUploadListener listener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images/" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String downloadUrl = downloadUri.toString();
                            Log.d("FirebaseServer", "Image uploaded successfully. URL: " + downloadUrl);
                            listener.onUploadSuccess(downloadUrl);  // Pass the URL back to MyApplication
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirebaseServer", "Failed to get download URL", e);
                            listener.onUploadFailure(e);
                        }))
                .addOnFailureListener(e -> {
                    Log.e("FirebaseServer", "Failed to upload image", e);
                    listener.onUploadFailure(e);
                });
    }

    public void uploadBitmapToFirebase(Bitmap bitmap, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
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

    @Override
    public void updateOrganizerInFirebase(Organizer organizer) {
//        if (isUpdatingFromLocal) {
        // Only update Firebase if there are real changes
//            if (!organizer.equals(organizerLiveData.getValue())) {
        isUpdatingFromLocal = false;  // Prevent recursive update loop

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection("organizers")
                .document(organizer.getId());

        docRef.set(organizer).addOnSuccessListener(aVoid -> {
            Log.d("FirebaseServer", "Organizer data successfully updated in Firebase");

            // Update the events subcollection if needed
            updateOrganizerEvents(organizer.getId(), organizer.getEvents());

            // After completing Firebase write, re-enable local updates
            isUpdatingFromLocal = true;

        }).addOnFailureListener(e -> {
            Log.w("FirebaseServer", "Error updating organizer data", e);
            isUpdatingFromLocal = true;  // Reset flag in case of failure
        });
//            }
//        }
    }


    @Override
    public void updateOrganizerEvents(String id, ArrayList<Event> events) {
        CollectionReference eventsRef = FirebaseFirestore.getInstance()
                .collection("organizers")
                .document(id)
                .collection("events");

        eventsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                doc.getReference().delete();
            }

            for (Event event : events) {
                eventsRef.document(event.getId()).set(event);
            }
            Log.d("FirebaseServer", "Organizer events successfully updated in Firebase");
        }).addOnFailureListener(e -> {
            Log.e("FirebaseServer", "Organizer events successfully updated in Firebase");
        });
    }

    @Override
    public void addToEventWaitingList(Event event, String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("organizers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot organizerDoc : queryDocumentSnapshots) {
                        String organizerId = organizerDoc.getId();

                        // Get the events subcollection for each organizer
                        db.collection("organizers")
                                .document(organizerId)
                                .collection("events")
                                .document(event.getId())
                                .get()
                                .addOnSuccessListener(eventDoc -> {
                                    if (eventDoc.exists()) {
                                        // If the event with targetEventId exists, add the new user to the userId subcollection
                                        db.collection("organizers")
                                                .document(organizerId)
                                                .collection("events")
                                                .document(event.getId())
                                                .collection("userId")
                                                .document(id)
                                                .set(new HashMap<String, Object>() {{
                                                    put("userId", id);
                                                }}) // You can add any field-value pairs here if necessary
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("addUserToEvent", "User ID added successfully to the event: " + event.getId());
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("addUserToEvent", "Failed to add user ID to the event: " + e.getMessage(), e);
                                                });
                                    } else {
                                        Log.d("addUserToEvent", "Event with ID " + event.getId() + " not found in organizer " + organizerId);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("addUserToEvent", "Failed to retrieve event: " + e.getMessage(), e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("addUserToEvent", "Failed to retrieve organizers: " + e.getMessage(), e);
                });
    }

    @Override
    public void removeFromEventWaitingList(Event event, String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("organizers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot organizerDoc : queryDocumentSnapshots) {
                        String organizerId = organizerDoc.getId();

                        // Get the events subcollection for each organizer
                        db.collection("organizers")
                                .document(organizerId)
                                .collection("events")
                                .document(event.getId())
                                .get()
                                .addOnSuccessListener(eventDoc -> {
                                    if (eventDoc.exists()) {
                                        // If the event with targetEventId exists, remove the user from the userId subcollection
                                        db.collection("organizers")
                                                .document(organizerId)
                                                .collection("events")
                                                .document(event.getId())
                                                .collection("userId")
                                                .document(id)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("removeUserFromEvent", "User ID removed successfully from the event: " + event.getId());
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("removeUserFromEvent", "Failed to remove user ID from the event: " + e.getMessage(), e);
                                                });
                                    } else {
                                        Log.d("removeUserFromEvent", "Event with ID " + event.getId() + " not found in organizer " + organizerId);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("removeUserFromEvent", "Failed to retrieve event: " + e.getMessage(), e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("removeUserFromEvent", "Failed to retrieve organizers: " + e.getMessage(), e);
                });
    }

//    public void joinEventWaitingList(Event event, String id) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("entrants")
//                .document(id)  // Assuming entrant has a unique ID
//                .collection("entrantWaitList")
//                .document(event.getId())
//                .set(new HashMap<String, Object>() {{
//                    put("eventId", event.getId());
//                }})
//                .addOnSuccessListener(aVoid -> {
//                    Log.d("join event waiting list", "Entrant data updated successfully in Firebase");
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("join event waiting list", "Failed to update entrant data in Firebase", e);
//                });
//    }

    @Override
    public void leaveEventWaitingList(Event event, String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("entrants")
                .document(id)  // Assuming entrant has a unique ID
                .collection("entrantWaitList")
                .document(event.getId())  // Specify the document to delete by its event ID
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("leave event waiting list", "Entrant data deleted successfully from Firebase");
                })
                .addOnFailureListener(e -> {
                    Log.e("leave event waiting list", "Failed to delete entrant data from Firebase", e);
                });
    }

    private void updateWaitlistEventIds(String userId, List<String> waitlistEventIds) {
        CollectionReference waitlistRef = FirebaseFirestore.getInstance()
                .collection("entrants")
                .document(userId)
                .collection("wishlistEventIds");

        // Clear the existing waitlistEventIds collection
        waitlistRef.get().addOnSuccessListener(querySnapshot -> {
            for (QueryDocumentSnapshot doc : querySnapshot) {
                doc.getReference().delete();
            }

            // Add each event ID in waitlistEventIds to the subcollection
            for (String eventId : waitlistEventIds) {
                waitlistRef.document(eventId).set(new HashMap<>());
            }

            Log.d("FirebaseServer", "WaitlistEventIds successfully updated in Firebase");

        }).addOnFailureListener(e -> {
            Log.w("FirebaseServer", "Error updating waitlistEventIds in Firebase", e);
        });
    }

    @Override
    public LiveData<Entrant> getEntrantLiveData() {
        return entrantLiveData;
    }

    @Override
    public LiveData<Organizer> getOrganizerLiveData() {
        return organizerLiveData;
    }

    @Override
    public FirebaseFirestore getDb() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }

    @Override
    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }
}

