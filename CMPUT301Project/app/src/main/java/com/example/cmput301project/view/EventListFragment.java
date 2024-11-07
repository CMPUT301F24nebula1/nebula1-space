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

import com.example.cmput301project.model.Organizer;
import com.example.cmput301project.view.EventListFragmentDirections;
import com.example.cmput301project.MyApplication;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.EventArrayAdapter;
import com.example.cmput301project.databinding.EventListBinding;
import com.example.cmput301project.model.Event;

import java.util.ArrayList;

/**
 * Fragment for organizer to view their events
 * @author Xinjia Fan
 */
public class EventListFragment extends Fragment {
    private EventListBinding binding;

    private ArrayList<Event> events;
    private ListView eventList;
    private EventArrayAdapter eventAdapter;

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

        eventList = binding.eventList;
        MyApplication app = (MyApplication) requireActivity().getApplication();

        Organizer o = app.getOrganizer();
        if (o != null) {
            // Update UI
            events = o.getEvents();
            Log.d("event list initial", String.valueOf(events.size()));
        }
        if (eventList != null && events != null) {
            // Set the adapter with the context and event data
            eventAdapter = new EventArrayAdapter(getContext(), events);
            eventList.setAdapter(eventAdapter);
        }
        app.getOrganizerLiveData().observe(getViewLifecycleOwner(), organizer -> {
            // Use the organizer data here
            if (organizer != null) {
                // Update UI
                events = organizer.getEvents();
                Log.d("event list livedata", String.valueOf(events.size()));
            }
            if (eventList != null && events != null) {
                // Set the adapter with the context and event data
                eventAdapter = new EventArrayAdapter(getContext(), events);
                eventList.setAdapter(eventAdapter);
            }
        });

        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    EventListFragmentDirections.ActionEventListToOrganizerEventDetail action = EventListFragmentDirections.actionEventListToOrganizerEventDetail(events.get(i));
                    NavHostFragment.findNavController(EventListFragment.this).navigate(action);
                }
            });

        binding.addEventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EventListFragment.this)
                        .navigate(R.id.action_EventList_to_AddEvent)
        );
    }
}
