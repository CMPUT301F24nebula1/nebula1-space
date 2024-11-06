package com.example.cmput301project.view;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
        String date;

        entrant = app.getEntrantLiveData().getValue();
        if (entrant != null) {
            ec = new EntrantController(entrant);
            if (entrant.getWaitlistEventIds().contains(e.getId())) {
                setButtonSelected(binding.leaveClassButton, binding.joinClassButton);
            }
            else {
                setButtonSelected(binding.joinClassButton, binding.leaveClassButton);
            }
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

        try {
            if (!e.getStartDate().isEmpty()) {
                date = e.getStartDate();
                if (!e.getEndDate().isEmpty()) {
                    date = date + '-' + e.getEndDate();
                }
                binding.registrationCloseDate.setText(date);
            }
        } catch (NullPointerException exception) {
            binding.registrationCloseDate.setVisibility(View.INVISIBLE);
        }

        binding.joinClassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!app.getEntrant().getWaitlistEventIds().contains(e.getId())) {
                    app.getEntrant().join_event(e);
                    e.add_entrant(app.getEntrant());

                    ec.joinEventWaitingList(e);

                    ec.addToEventWaitingList(e);
                    setButtonSelected(binding.leaveClassButton, binding.joinClassButton);
                    Log.d("join event", "You join the waiting list.");
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
                Log.d("leave event", "You leave the wishlist");
                ec.removeFromEventWaitingList(e);
                setButtonSelected(binding.joinClassButton, binding.leaveClassButton);
            }
            else {
                Log.d("leave event", "you are not in the waitlist");
            }
        });
    }
    private void setButtonSelected(Button selectedButton, Button unselectedButton) {
        // Set the background of the selected button to the solid style
        selectedButton.setBackgroundResource(R.drawable.rounded_button_join_solid); // or the relevant solid background
        selectedButton.setEnabled(true);
        selectedButton.setAlpha(1f);
        selectedButton.setTextColor(Color.parseColor("#FFFFFF"));

        // Set the background of the unselected button to the outline style
        unselectedButton.setBackgroundResource(R.drawable.rounded_button_leave_outline); // or the relevant outline background
        unselectedButton.setEnabled(false);
        unselectedButton.setAlpha(0.5f);
        unselectedButton.setTextColor(Color.parseColor("#65558F"));

    }
}
