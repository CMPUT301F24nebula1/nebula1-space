package com.example.cmput301project.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
//import com.example.cmput301project.Manifest;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.MyApplication;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.EntrantController;
import com.example.cmput301project.databinding.EntrantEventViewBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
    String category;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        binding = EntrantEventViewBinding.inflate(inflater, container, false);
        e = EntrantEventViewFragmentArgs.fromBundle(getArguments()).getE();
        category = EntrantEventViewFragmentArgs.fromBundle(getArguments()).getCategory();

        if (e.isRequiresGeolocation()) {
            binding.geolocationContainer.setVisibility(View.VISIBLE);
            binding.geolocationWarning.setText("This event utilizes geolocation.");
            binding.geolocationCheckbox.setChecked(true);
            binding.geolocationCheckbox.setEnabled(false);
        } else {
            binding.geolocationContainer.setVisibility(View.GONE);
        }
        // Customize UI based on category
        switch (category) {
            case "SELECTED":
                // Handle Pending events
                binding.joinClassButton.setText("Accept");
                binding.leaveClassButton.setText("Decline");

                break;
            case "CANCELED":
            case "FINAL":
                // Handle Canceled/Declined events
                binding.joinClassButton.setVisibility(View.GONE);
                binding.leaveClassButton.setVisibility(View.GONE);
                break;
            default:
                // Handle default case
                binding.joinClassButton.setText("Join Class");
                binding.leaveClassButton.setText("Leave Class");
                break;
        }
        if (e.isFinalized()) {
            binding.joinClassButton.setVisibility(View.GONE);
            binding.leaveClassButton.setVisibility(View.GONE);
            binding.eventFinalizedText.setVisibility(View.VISIBLE);
        }
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
                binding.eventPosterImageview.setVisibility(View.VISIBLE);
                Glide.with(getContext())
                        .load(e.getPosterUrl())
                        .placeholder(R.drawable.placeholder_image)  // placeholder
                        .error(R.drawable.error_image)              // error image
                        .into(binding.eventPosterImageview);
            } else {
                binding.eventPosterImageview.setVisibility(View.GONE);
            }
        } catch (NullPointerException exception) {
            binding.eventPosterImageview.setVisibility(View.GONE);
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

        //helper function added at button of fragment called proceedToJoinEvent
        binding.joinClassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("wishlist click listener", app.getEntrant().getWaitlistEventIds().toString());
                if (category.equals("WAITING")) {
                    if (!app.getEntrant().getWaitlistEventIds().contains(e.getId())) {
                        try {
                            if (app.getEntrant().getName().isEmpty() || app.getEntrant().getEmail().isEmpty()) {
                                Toast.makeText(getContext(), "You cannot join an event waitlist without providing your name and email.!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (NullPointerException e) {
                            Toast.makeText(getContext(), "You cannot join an event waitlist without providing your name and email.!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (e.getLimit() > 0 && e.getWaitlistEntrantIds().size() >= e.getLimit()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setTitle("Information")
                                    .setMessage("This event is full.")
                                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                    .setCancelable(false)  // prevents dialog from closing on back press
                                    .show();
                        } else if (e.isRequiresGeolocation()) {

                            // Check if location permission is granted
                            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // Request location permission if not granted
                                ActivityCompat.requestPermissions(requireActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_PERMISSION_REQUEST_CODE);
                                return; // Exit the method to wait for user permission
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setTitle("Geolocation Required");
                            builder.setMessage("This event utilizes geolocation. Are you sure you wish to proceed?");

                            //"Proceed" Button
                            builder.setPositiveButton("Proceed", (dialog, which) -> proceedToJoinEvent());

                            //"Cancel" Button
                            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                            builder.create().show();
                        } else {
                            //direct to join event
                            proceedToJoinEvent();
                        }

                    } else {
                        Log.d("join event", "you are already in the waitlist");
                        Toast.makeText(getContext(), "You are already in the waitlist!", Toast.LENGTH_SHORT).show();
                        setButtonSelected(binding.leaveClassButton, binding.joinClassButton);
                    }
                }
                else if (category.equals("SELECTED")) {
                    proceedToAcceptEvent();
                }
            }
        });

        binding.leaveClassButton.setOnClickListener(view1 -> {
            if (category.equals("WAITING")) {
                if (app.getEntrant().getWaitlistEventIds().contains(e.getId())) {
                    app.getEntrant().leave_event(e);
                    e.remove_entrant(app.getEntrant());

                    ec.leaveEventWaitingList(e);
                    Log.d("leave event", "You leave the wishlist");
                    Toast.makeText(getContext(), "You leaved the waiting list!", Toast.LENGTH_SHORT).show();
                    ec.removeFromEventWaitingList(e);
                    setButtonSelected(binding.joinClassButton, binding.leaveClassButton);
                } else {
                    Log.d("leave event", "you are not in the waitlist");
                    Toast.makeText(getContext(), "You are not in the waiting list!", Toast.LENGTH_SHORT).show();
                    setButtonSelected(binding.joinClassButton, binding.leaveClassButton);
                }
            }
            else if (category.equals("SELECTED")) {
                proceedToDeclineEvent();
            }
        });
    }

    private void setButtonSelected(Button selectedButton, Button unselectedButton) {

        switch (category) {
            case "SELECTED": {
                // Set the background of the selected button to the solid style
                binding.joinClassButton.setBackgroundResource(R.drawable.rounded_button_join_solid); // or the relevant solid background
                binding.joinClassButton.setEnabled(true);
                binding.joinClassButton.setAlpha(1f);
                binding.joinClassButton.setTextColor(Color.parseColor("#FFFFFF"));

                // Set the background of the unselected button to the outline style
                binding.leaveClassButton.setBackgroundResource(R.drawable.rounded_button_join_solid); // or the relevant outline background
                binding.leaveClassButton.setEnabled(true);
                binding.leaveClassButton.setAlpha(1f);
                binding.leaveClassButton.setTextColor(Color.parseColor("#FFFFFF"));
                return;
            }
            case "CANCELED":
            case "FINAL":
                // Handle Canceled/Declined events
                binding.joinClassButton.setVisibility(View.GONE);
                binding.leaveClassButton.setVisibility(View.GONE);
                return;
            default:
                break;
        }
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

    private void proceedToJoinEvent() {
        Log.d("join event", app.getEntrant().getWaitlistEventIds().toString());
        app.getEntrant().join_event(e);       // Add entrant to the event's waitlist
        e.add_entrant(app.getEntrant());      // Update the event's entrant list
        ec.joinEventWaitingList(e, requireContext());           // Sync changes with the database
        ec.addToEventWaitingList(e);          // Update local storage
        setButtonSelected(binding.leaveClassButton, binding.joinClassButton); // Update UI
        Toast.makeText(getContext(), "You joined the waiting list!", Toast.LENGTH_SHORT).show();
    }

    private void proceedToAcceptEvent() {
        lockUI();
        updateStatus(entrant, "FINAL", e.getId(), new UpdateStatusCallback() {
            @Override
            public void onSuccess() {
                unlockUI();
                Toast.makeText(getContext(), "You accepted the invitation!", Toast.LENGTH_SHORT).show();
                binding.joinClassButton.setVisibility(View.GONE);
                binding.leaveClassButton.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                unlockUI();
                Toast.makeText(getContext(), "Failed to accept.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNotFound() {
                unlockUI();
                Toast.makeText(getContext(), "Event not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void  proceedToDeclineEvent() {
        lockUI();
        updateStatus(entrant, "CANCELED", e.getId(), new UpdateStatusCallback() {
            @Override
            public void onSuccess() {
                unlockUI();
                Toast.makeText(getContext(), "You declined the invitation!", Toast.LENGTH_SHORT).show();
                binding.joinClassButton.setVisibility(View.GONE);
                binding.leaveClassButton.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                unlockUI();
                Toast.makeText(getContext(), "Failed to decline.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNotFound() {
                unlockUI();
                Toast.makeText(getContext(), "Event not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface UpdateStatusCallback {
        void onSuccess();  // Called when the status update is successful
        void onFailure(Exception e);  // Called when the status update fails
        void onNotFound();  // Called when no matching document is found
    }

    public void updateStatus(Entrant entrant, String status, String eventId, UpdateStatusCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference waitlistRef = db.collection("entrants")
                .document(entrant.getId())
                .collection("entrantWaitList");

        Log.d("notification eventId", eventId);
        waitlistRef.whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            DocumentReference docRef = document.getReference();
                            // Update the status field
                            docRef.update("status", status)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "Status field updated successfully");
                                        if (callback != null) {
                                            callback.onSuccess();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error updating status field", e);
                                        if (callback != null) {
                                            callback.onFailure(e);
                                        }
                                    });
                        }
                    } else {
                        Log.d("Firestore", "No document found with the specified eventId");
                        if (callback != null) {
                            callback.onNotFound();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error finding document", e);
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed to the geolocation dialog
                proceedToJoinEvent();
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(requireContext(), "Location permission is required to join this event.", Toast.LENGTH_SHORT).show();
            }
        }
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
