package com.example.cmput301project.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.MyApplication;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.CategorizedEventAdapter;
import com.example.cmput301project.controller.EventArrayAdapter;
import com.example.cmput301project.databinding.EventListBinding;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holding global variables
 * @author Xinjia Fan
 */
public class EntrantClassFragment extends Fragment {
    private EventListBinding binding;

    private ArrayList<Event> events;
    private ListView eventList;
    CategorizedEventAdapter adapter;

    private ArrayList<String> wishlistEventIds;

    private Entrant entrant;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        binding = EventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.addEventButton.setVisibility(View.GONE);

        eventList = binding.eventList;
        events = new ArrayList<Event>();
        MyApplication app = (MyApplication) requireActivity().getApplication();

        entrant = app.getEntrantLiveData().getValue();

        app.getEntrantLiveData().observe(getViewLifecycleOwner(), entrant1 -> {
            entrant = entrant1;
            wishlistEventIds = entrant.getWaitlistEventIds();

            lockUI();
            retrieveEvents(entrant, new EventsCallback() {
                @Override
                public void onEventsRetrieved(ArrayList<Event> events) {
                    unlockUI();
                    listenToEntrantWaitlist(entrant.getId(), new WaitlistCallback() {
                        @Override
                        public void onWaitlistUpdated(Map<String, String> waitlistMap) {
                            // Group events by status
                            Map<String, ArrayList<Event>> groupedEvents = groupEventsByStatus(events, waitlistMap);
                            Map<String, ArrayList<Event>> sortedEvents = sortCategorizedEvents(groupedEvents);

                            // Use the categorized adapter that reuses EventArrayAdapter
//                            CategorizedEventAdapter categorizedAdapter = new CategorizedEventAdapter(getContext(), sortedEvents);
//                            eventList.setAdapter(categorizedAdapter);

                            if (eventList.getAdapter() instanceof CategorizedEventAdapter) {
                                CategorizedEventAdapter adapter = (CategorizedEventAdapter) eventList.getAdapter();
                                adapter.updateData(sortedEvents);
                            } else {
                                CategorizedEventAdapter categorizedAdapter = new CategorizedEventAdapter(getContext(), sortedEvents);
                                eventList.setAdapter(categorizedAdapter);
                            }
                        }
                    });

                }

                @Override
                public void emptyEvents() {
                    unlockUI();
                    Toast.makeText(getContext(), "No classes!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        eventList.setOnItemClickListener((adapterView, view1, position, id) -> {
            Object item = eventList.getAdapter().getItem(position);
            String category = "";
            if (item instanceof Event) {
                Event clickedEvent = (Event) item;

                CategorizedEventAdapter adapter = (CategorizedEventAdapter) eventList.getAdapter();
                Map<String, ArrayList<Event>> categorizedEvents = adapter.getCategorizedEvents();

                for (Map.Entry<String, ArrayList<Event>> entry : categorizedEvents.entrySet()) {
                    if (entry.getValue().contains(clickedEvent)) {
                        category = entry.getKey();
                        break;
                    }
                }

//                String category = "SELECTED"; // Replace with the appropriate category logic

                EntrantClassFragmentDirections.ActionEntrantClassToEntrantEventView action =
                        EntrantClassFragmentDirections.actionEntrantClassToEntrantEventView((Event) item, category);
                NavHostFragment.findNavController(EntrantClassFragment.this).navigate(action);
            }
        });
    }

    private Map<String, ArrayList<Event>> groupEventsByStatus(ArrayList<Event> events, Map<String, String> waitlistMap) {
        Map<String, ArrayList<Event>> categorizedEvents = new LinkedHashMap<>();

        for (Event event : events) {
            String status = waitlistMap.get(event.getId());
            if (!categorizedEvents.containsKey(status)) {
                categorizedEvents.put(status, new ArrayList<>());
            }
            if (status.equals("WAITING") && event.isFinalized())
                continue;
            categorizedEvents.get(status).add(event);
        }

        for (Map.Entry<String, ArrayList<Event>> entry : categorizedEvents.entrySet()) {
            entry.getValue().sort((event1, event2) -> event1.getName().compareToIgnoreCase(event2.getName()));
        }

        return categorizedEvents;
    }

    private Map<String, ArrayList<Event>> sortCategorizedEvents(Map<String, ArrayList<Event>> categorizedEvents) {
        // Define the desired order of the keys
        List<String> sortedKeys = Arrays.asList("WAITING", "WAITING1", "SELECTED", "CANCELED", "FINAL");

        // Create a new LinkedHashMap to maintain the sorted order
        Map<String, ArrayList<Event>> sortedMap = new LinkedHashMap<>();

        // Add the keys to the new map in the specified order
        for (String key : sortedKeys) {
            if (categorizedEvents.containsKey(key)) {
                sortedMap.put(key, categorizedEvents.get(key));
            }
        }

        return sortedMap;
    }


    public interface EventsCallback {
        void onEventsRetrieved(ArrayList<Event> events);
        void emptyEvents();
    }

    public void retrieveEvents(Entrant entrant, EventsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<String> wishlistEventIds = entrant.getWaitlistEventIds();

        events.clear();

        if (wishlistEventIds.isEmpty()) {
            Log.d("entrant class", "debug1");
            callback.emptyEvents();
            return;
        }

        // Counter for processed event IDs
        AtomicInteger processedCount = new AtomicInteger(0);
        int totalEventIds = wishlistEventIds.size();

        for (String eventId : wishlistEventIds) {
            Log.d("entrant class", eventId);

            // Query all organizers for the current event ID
            db.collection("organizers").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    AtomicInteger organizerProcessedCount = new AtomicInteger(0);
                    boolean[] eventFound = {false}; // Use an array to allow modification in inner classes

                    for (QueryDocumentSnapshot organizerDoc : task.getResult()) {
                        // Access the "events" subcollection for each organizer
                        db.collection("organizers").document(organizerDoc.getId())
                                .collection("events").document(eventId)
                                .get()
                                .addOnCompleteListener(eventTask -> {
                                    organizerProcessedCount.incrementAndGet(); // Track organizer checks for this eventId

                                    if (eventTask.isSuccessful() && eventTask.getResult() != null && eventTask.getResult().exists()) {
                                        if (!eventFound[0]) { // Ensure we only add the event once
                                            Event event = eventTask.getResult().toObject(Event.class);
                                            events.add(event);
                                            eventFound[0] = true;
                                        }
                                    }

                                    // Once all organizers are processed for this eventId
                                    if (organizerProcessedCount.get() == task.getResult().size()) {
                                        processedCount.incrementAndGet(); // Increment only once for this eventId
                                        if (processedCount.get() == totalEventIds) {
                                            if (events.isEmpty()) {
                                                Log.d("entrant class", "debug2");
                                                callback.emptyEvents(); // No valid events found
                                            } else {
                                                events.sort((e1, e2) -> {
                                                    if (e1.getTimestamp() == null && e2.getTimestamp() == null) return 0;
                                                    if (e1.getTimestamp() == null) return 1; // e1 comes after e2
                                                    if (e2.getTimestamp() == null) return -1; // e2 comes after e1
                                                    return e2.getTimestamp().compareTo(e1.getTimestamp()); // Compare non-null timestamps
                                                });
                                                callback.onEventsRetrieved(events); // Found events
                                            }
                                        }
                                    }
                                });
                    }
                } else {
                    // If the organizer query fails, treat this eventId as processed
                    processedCount.incrementAndGet();
                    if (processedCount.get() == totalEventIds) {
                        if (events.isEmpty()) {
                            Log.d("entrant class", "debug3");
                            callback.emptyEvents();
                        } else {
                            events.sort((e1, e2) -> {
                                if (e1.getTimestamp() == null && e2.getTimestamp() == null) return 0;
                                if (e1.getTimestamp() == null) return 1; // e1 comes after e2
                                if (e2.getTimestamp() == null) return -1; // e2 comes after e1
                                return e1.getTimestamp().compareTo(e2.getTimestamp()); // Compare non-null timestamps
                            });
                            callback.onEventsRetrieved(events);
                        }
                    }
                    Log.d("Firebase", "Error getting organizers: ", task.getException());
                }
            });
        }
    }



    public interface WaitlistCallback {
        void onWaitlistUpdated(Map<String, String> waitlistMap);
    }

    private void listenToEntrantWaitlist(String entrantId, WaitlistCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference waitlistRef = db.collection("entrants")
                .document(entrantId)
                .collection("entrantWaitList");

        Map<String, String> waitlistMap = new HashMap<>();

        waitlistRef.addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Log.e("FirestoreError", "Error listening for waitlist updates", error);
                return;
            }

            if (querySnapshot != null) {
                waitlistMap.clear(); // Clear the map to avoid duplicate entries

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    String eventId = document.getString("eventId");
                    String status = document.getString("status");

                    if (eventId != null && status != null) {
                        waitlistMap.put(eventId, status); // Update the map
                    }
                }

                // Log the updated waitlist for debugging
                waitlistMap.forEach((key, value) -> {
                    Log.d("Waitlist", "EventId: " + key + ", Status: " + value);
                });

                // Trigger the callback with the updated waitlist
                callback.onWaitlistUpdated(waitlistMap);
            }
        });
    }

    private void lockUI() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.mainLayout.setAlpha(0.5f); // Dim background for effect
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void unlockUI() {
        binding.progressBar.setVisibility(View.GONE);
        binding.mainLayout.setAlpha(1.0f); // Restore background opacity
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}
