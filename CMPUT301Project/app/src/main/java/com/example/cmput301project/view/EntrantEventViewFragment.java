package com.example.cmput301project.view;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cmput301project.model.Entrant;
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
        Log.d("waitlist entrants", e.getName() + ' ' + String.valueOf(e.getWaitlistEntrantIds().size()));
        Log.d("waitlist limit", e.getName() + String.valueOf(e.getLimit()));
        app = (MyApplication) requireActivity().getApplication();

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        String date;

        entrant = app.getEntrant();
        if (entrant != null) {
            ec = new EntrantController(entrant);
            Log.d("Wishlist", "Wishlist items: " + entrant.getWaitlistEventIds().toString());
            if (entrant.getWaitlistEventIds().contains(e.getId()) || e.getWaitlistEntrantIds().contains(entrant)) {
                setButtonSelected(binding.leaveClassButton, binding.joinClassButton);
                Log.d("wishlist", "1");
            }
            else {
                setButtonSelected(binding.joinClassButton, binding.leaveClassButton);
                Log.d("wishlist", "2");
            }
        }

        app.getEntrantLiveData().observe(getViewLifecycleOwner(), entrant1 -> {
            if (entrant1 != null) {
                entrant = entrant1;
                ec = new EntrantController(entrant);
                Log.d("entrant event myapplication", entrant1.getWaitlistEventIds().toString());
                if (entrant.getWaitlistEventIds().contains(e.getId())) {
                    setButtonSelected(binding.leaveClassButton, binding.joinClassButton);
//                    Log.d("wishlist1", "1");
                }
                else {
                    setButtonSelected(binding.joinClassButton, binding.leaveClassButton);
//                    Log.d("wishlist1", "2");
                }
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
                binding.eventDescription.setText("");
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
                Log.d("wishlist click listener", app.getEntrant().getWaitlistEventIds().toString());
                if (!app.getEntrant().getWaitlistEventIds().contains(e.getId())) {

                    if (e.getLimit() > 0 && e.getWaitlistEntrantIds().size() >= e.getLimit()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        builder.setTitle("Information")
                                .setMessage("This event is full.")
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .setCancelable(false)  // prevents dialog from closing on back press
                                .show();
                    } else {
                        Log.d("join event", app.getEntrant().getWaitlistEventIds().toString());
                        app.getEntrant().join_event(e);
                        e.add_entrant(app.getEntrant());

                        ec.joinEventWaitingList(e);

                        ec.addToEventWaitingList(e);
                        setButtonSelected(binding.leaveClassButton, binding.joinClassButton);
                        Log.d("join event", "You join the waiting list.");
                        Toast.makeText(getContext(), "You joined the waiting list!", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Log.d("join event", "you are already in the waitlist");
                    Toast.makeText(getContext(), "You are already in the waitlist!", Toast.LENGTH_SHORT).show();
                    setButtonSelected(binding.leaveClassButton, binding.joinClassButton);
                }
            }
        });

        binding.leaveClassButton.setOnClickListener(view1 -> {
            if (app.getEntrant().getWaitlistEventIds().contains(e.getId())) {
                app.getEntrant().leave_event(e);
                e.remove_entrant(app.getEntrant());

                ec.leaveEventWaitingList(e);
                Log.d("leave event", "You leave the wishlist");
                Toast.makeText(getContext(), "You leaved the waiting list!", Toast.LENGTH_SHORT).show();
                ec.removeFromEventWaitingList(e);
                setButtonSelected(binding.joinClassButton, binding.leaveClassButton);
            }
            else {
                Log.d("leave event", "you are not in the waitlist");
                Toast.makeText(getContext(), "You are not in the waiting list!", Toast.LENGTH_SHORT).show();
                setButtonSelected(binding.joinClassButton, binding.leaveClassButton);
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
