package com.example.cmput301project;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.cmput301project.databinding.EntrantEventViewBinding;
import com.example.cmput301project.databinding.EntrantHomepageBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class EntrantEventViewFragment extends Fragment {
    private EntrantEventViewBinding binding;
    MyApplication app;
    Event e;
    EntrantController ec;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        binding = EntrantEventViewBinding.inflate(inflater, container, false);
        e = OrganizerEventDetailFragmentArgs.fromBundle(getArguments()).getE();
        app = (MyApplication) requireActivity().getApplication();
        ec = new EntrantController(app.getEntrant());
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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
                if (!app.getEntrant().getWaitlistEventIds().contains(e.getId())) {
                    app.getEntrant().join_event(e);
                    e.add_entrant(app.getEntrant());

                    ec.joinEventWaitingList(e);

                    ec.addToEventWaitingList(e);

                }
                else {
                    Log.d("join event", "you are already in the waitlist");
                }
            }
        });

        binding.leaveClassButton.setOnClickListener(view1 -> {
            if (app.getEntrant().getWaitlistEventIds().contains(e.getId())) {
                app.getEntrant().leave_event(e);
                e.remove_entrant(app.getEntrant());

                ec.leaveEventWaitingList(e);
                ec.removeFromEventWaitingList(e);
            }
            else {
                Log.d("leave event", "you are not in the waitlist");
            }
        });
    }
}
