package com.example.cmput301project.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.view.EntrantEventViewFragmentArgs;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.MyApplication;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.EntrantController;
import com.example.cmput301project.databinding.EntrantEventViewBinding;

/**
 * Fragment for entrants to view an event after scanning the QR code
 * @author Xinjia Fan
 */
public class EntrantEventViewFragment extends Fragment {
    private EntrantEventViewBinding binding;
    MyApplication app;
    Event e;
    EntrantController ec;
    Entrant entrant;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        binding = EntrantEventViewBinding.inflate(inflater, container, false);
        e = EntrantEventViewFragmentArgs.fromBundle(getArguments()).getE();
        app = (MyApplication) requireActivity().getApplication();

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        entrant = app.getEntrantLiveData().getValue();
        if (entrant != null) {
            ec = new EntrantController(entrant);
        }

        app.getEntrantLiveData().observe(getViewLifecycleOwner(), entrant1 -> {
            if (entrant1 != null) {
                entrant = entrant1;
                ec = new EntrantController(entrant);
                Log.d("entrant event myapplication", entrant1.getWaitlistEventIds().toString());
            }
        });

        binding.eventName.setText(e.getName());

        try {
            if (!e.getPosterUrl().isEmpty()) {
                Glide.with(getContext())
                        .load(e.getPosterUrl())
                        .placeholder(R.drawable.placeholder_image)  // placeholder
                        .error(R.drawable.error_image)              // error image
                        .into(binding.eventPosterImageview);
            }
        } catch (NullPointerException exception) {
            Log.e("Error", "Poster URL is null", exception);
        }

        try {
            if (!e.getDescription().isEmpty()) {
                binding.eventDescription.setText(e.getDescription());
            }
            else {
                String error = "No description";
                binding.eventDescription.setText(error);
            }
        } catch (NullPointerException exception) {
            Log.e("Error", "Description is null", exception);
            String error = "No description";
            binding.eventDescription.setText(error);
        }

        binding.joinClassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("entrant event", entrant.getWaitlistEventIds().toString());
                if (!entrant.getWaitlistEventIds().contains(e.getId())) {

                    if (entrant != null) {
                        //ec.joinEventWaitingList(e);
                        entrant.join_event(e);
                        app.setEntrantLiveData(entrant);
                        Log.d("join event", "You join the wishlist!");
                        e.add_entrant(entrant);
                        ec.addToEventWaitingList(e);

                    }

                }
                else {
                    Log.d("join event", "you are already in the waitlist");
                }
            }
        });

        binding.leaveClassButton.setOnClickListener(view1 -> {
            if (entrant.getWaitlistEventIds().contains(e.getId())) {
                ec.leaveEventWaitingList(e);
                e.remove_entrant(entrant);
                Log.d("leave event", "You leave the wishlist");
                app.setEntrantLiveData(entrant);
                ec.removeFromEventWaitingList(e);
            }
            else {
                Log.d("leave event", "you are not in the waitlist");
            }
        });
    }
}
