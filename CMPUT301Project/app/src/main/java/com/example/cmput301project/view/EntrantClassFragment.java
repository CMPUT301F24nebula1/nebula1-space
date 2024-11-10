package com.example.cmput301project.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.MyApplication;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.EventArrayAdapter;
import com.example.cmput301project.databinding.EventListBinding;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EntrantClassFragment extends Fragment {
    private EventListBinding binding;

    private ArrayList<Event> events;
    private ListView eventList;
    private EventArrayAdapter eventAdapter;

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
        MyApplication app = (MyApplication) requireActivity().getApplication();

        events = new ArrayList<Event>();
        eventAdapter = new EventArrayAdapter(getContext(), events);
        eventList.setAdapter(eventAdapter);

        entrant = app.getEntrantLiveData().getValue();

        app.getEntrantLiveData().observe(getViewLifecycleOwner(), entrant1 -> {
            // Use the organizer data here
            entrant = entrant1;
            wishlistEventIds = entrant.getWaitlistEventIds();
            Log.d("entrant wishlist", String.valueOf(wishlistEventIds.size()));

            retrieveEvents(entrant, new EventsCallback() {
                @Override
                public void onEventsRetrieved(ArrayList<Event> events) {
                    // Do something with the retrieved events, e.g., update UI
                    for (Event event : events) {
                        Log.d("Event", "Retrieved event: " + event.getName());
                    }
                    if (eventList != null && events != null) {
                        eventAdapter.notifyDataSetChanged();
                    }
                }
            });
        });

//        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                EventListFragmentDirections.ActionEventListToOrganizerEventDetail action = EventListFragmentDirections.actionEventListToOrganizerEventDetail(events.get(i));
//                NavHostFragment.findNavController(EventListFragment.this).navigate(action);
//            }
//        });

    }

    public interface EventsCallback {
        void onEventsRetrieved(ArrayList<Event> events);
    }

    public void retrieveEvents(Entrant entrant, EventsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<String> wishlistEventIds = entrant.getWaitlistEventIds();

        events.clear();

        // Loop through each event ID in the wishlist
        for (String eventId : wishlistEventIds) {
            // Query each organizer's "events" subcollection for an event with the matching ID
            db.collection("organizers")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot organizerDoc : task.getResult()) {
                                // Access the "events" subcollection for each organizer
                                db.collection("organizers").document(organizerDoc.getId())
                                        .collection("events").document(eventId)
                                        .get()
                                        .addOnCompleteListener(eventTask -> {
                                            if (eventTask.isSuccessful() && eventTask.getResult() != null && eventTask.getResult().exists()) {
                                                Event event = eventTask.getResult().toObject(Event.class);
                                                events.add(event); // Add the found event to the list
                                                if (events.size() == wishlistEventIds.size()) {
//                                                    eventAdapter.notifyDataSetChanged();
                                                    Log.d("My class after retrieve", events.toString());
                                                    callback.onEventsRetrieved(events); // Trigger callback when done
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d("Firebase", "Error getting event documents: ", task.getException());
                        }
                    });
        }

    }
}
