//code review from ChatGPT
package com.example.cmput301project.service;

import android.os.Bundle;
import android.util.Log;

import com.example.cmput301project.R;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// this part is for the pooling service, it will draw the entrants and listen for declines
public class PoolingService {

    private static final String TAG = "PoolingService";
    private final FirebaseFirestore db;
    private Organizer organizer;

    public PoolingService() {
        this.db = FirebaseFirestore.getInstance();
    }
    // this part is for the draw entrants, it will get the event and perform pooling
    public void drawEntrants(String eventId) {
        CollectionReference eventsRef = db.collection("organizers").document(organizer.getId())
                .collection("events");

        Log.d(TAG, "drawEntrants called with eventId: " + eventId);
        eventsRef.document(eventId)
//        db.collection("events").document(eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot eventDoc = task.getResult();
                        if (eventDoc.exists()) {
                            Event event = eventDoc.toObject(Event.class);

                            ArrayList<String> userIdList = new ArrayList<>();

                            eventsRef.document(event.getId()).collection("userId").get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                                            // field "userId" that stores a string
                                            String userId = document.getString("userId");
                                            if (userId != null) {
                                                userIdList.add(userId);
                                            }
                                        }
                                        event.setWaitlistEntrantIds(userIdList);
                                        Log.d("wishlist of event", event.getWaitlistEntrantIds().toString());
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle any errors here
                                        Log.e("FirestoreError", "Error retrieving user IDs", e);
                                    });


                            if (event != null) {
                                Log.d(TAG, "Event retrieved successfully: " + event.getId());
                                performPooling(event);
                            } else {
                                Log.e(TAG, "Event conversion failed.");
                            }
                        } else {
                            Log.e(TAG, "Event not found.");
                        }
                    } else {
                        Log.e(TAG, "Error getting event: ", task.getException());
                    }
                });
    }
    // this part is for the perform pooling, it will get the waitlist and select the entrants
    private void performPooling(Event event) {
        Log.d(TAG, "performPooling called for eventId: " + event.getId());
        db.collection("waitingList")
                .whereEqualTo("eventId", event.getId())
                .whereEqualTo("status", "WAITING")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot waitlistSnapshot = task.getResult();
                        List<Entrant> waitlist = new ArrayList<>();
                        for (DocumentSnapshot doc : waitlistSnapshot.getDocuments()) {
                            Entrant entrant = doc.toObject(Entrant.class);
                            if (entrant != null) {
                                entrant.setId(doc.getId());
                                waitlist.add(entrant);
                            }
                        }

                        Log.d(TAG, "Waitlist size: " + waitlist.size());

                        if (waitlist.isEmpty()) {
                            Log.e(TAG, "No entrants in the waitlist.");
                            return;
                        }

                        // shouldn't be event.getLimit(), should be another number specified by organizer
                        int capacity = event.getLimit();
                        Log.d(TAG, "Event capacity: " + capacity);

                        if (capacity <= 0) {
                            Log.e(TAG, "Invalid event capacity.");
                            return;
                        }

                        Collections.shuffle(waitlist);
                        List<Entrant> selectedEntrants = waitlist.stream()
                                .limit(capacity)
                                .collect(Collectors.toList());

                        Log.d(TAG, "Selected entrants count: " + selectedEntrants.size());

                        for (Entrant entrant : selectedEntrants) {
                            notifyEntrant(entrant, event);
                            updateEntrantStatus(entrant.getId(), "SELECTED");
                        }

                        listenForDeclines(event.getId());

                    } else {
                        Log.e(TAG, "Error getting waitlist: ", task.getException());
                    }
                });
    }
    // this part is for the notify entrant, it will notify the entrant
    private void notifyEntrant(Entrant entrant, Event event) {
        Log.d(TAG, "notifyEntrant called for entrantId: " + entrant.getId());

    }
    // this part is for the update entrant status, it will update the entrant status
    private void updateEntrantStatus(String entrantId, String status) {
        Log.d(TAG, "Updating entrant " + entrantId + " status to " + status);
        db.collection("waitingList").document(entrantId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Entrant " + entrantId + " status updated to " + status))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating entrant status: " + entrantId, e));
    }
    // this part is for the listen for declines, it will listen for the declines
    public void listenForDeclines(String eventId) {
        Log.d(TAG, "listenForDeclines called for eventId: " + eventId);
        db.collection("waitingList")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "DECLINED")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String entrantId = doc.getId();
                            Log.d(TAG, "Entrant " + entrantId + " has declined.");
                        }
                    }
                });
    }
}
