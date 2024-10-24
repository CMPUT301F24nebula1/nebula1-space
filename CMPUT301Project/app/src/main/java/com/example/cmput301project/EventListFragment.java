package com.example.cmput301project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.databinding.AddEventBinding;
import com.example.cmput301project.databinding.EventListBinding;

import java.util.ArrayList;

public class EventListFragment extends Fragment {
    private EventListBinding binding;

    private ArrayList<Event> events;
    private ListView eventList;
    private EventArrayAdapter eventAdapter;



    private SharedViewModel sharedViewModel;

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

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        eventList = binding.eventList;

        sharedViewModel.getOrganizerLiveData().observe(getViewLifecycleOwner(), organizer -> {
            if (organizer != null) {
                // Update the UI with the organizer data
                events = organizer.getEvents();
            }
            if (eventList != null && events != null) {
                // Set the adapter with the context and event data
                eventAdapter = new EventArrayAdapter(getContext(), events);
                eventList.setAdapter(eventAdapter);
            }
        });



        binding.addEventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EventListFragment.this)
                        .navigate(R.id.action_EventList_to_AddEvent)
        );
    }
}
