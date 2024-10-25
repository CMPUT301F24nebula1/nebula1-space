package com.example.cmput301project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.cmput301project.databinding.OrganizerEventDetailBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OrganizerEventDetailFragment extends Fragment {
    OrganizerEventDetailBinding binding;
    private FirebaseFirestore db;
    private Event e;
    MyApplication app;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = OrganizerEventDetailBinding.inflate(inflater, container, false);
        e = OrganizerEventDetailFragmentArgs.fromBundle(getArguments()).getE();
        app = (MyApplication) requireActivity().getApplication();
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();


        app.getOrganizerLiveData().observe(getViewLifecycleOwner(), organizer -> {
            // Use the organizer data here
            if (organizer != null) {
                // Use the organizer data (e.g., set organizer-related data in the UI)
                Log.d("Organizer", "Organizer Name: " + organizer.getName());

                ImageView i1 = binding.eventPosterImageview;
                ImageView i2 = binding.eventQrcodeImageview;
                TextView t1 = binding.descriptionTextview;
                TextView t2 = binding.eventNameTextview;

                try {
                    if (!e.getQrCode().isEmpty()) {
                        Glide.with(getContext())
                                .load(e.getQrCode())
                                .placeholder(R.drawable.placeholder_image)  // placeholder
                                .error(R.drawable.error_image)              // error image
                                .into(i2);
                    }
                } catch (NullPointerException exception) {
                    Log.e("Error", "Poster URL is null", exception);
                }

                try {
                    if (!e.getPosterUrl().isEmpty()) {
                        Glide.with(getContext())
                                .load(e.getPosterUrl())
                                .placeholder(R.drawable.placeholder_image)  // placeholder
                                .error(R.drawable.error_image)              // error image
                                .into(i1);
                    }
                } catch (NullPointerException exception) {
                    Log.e("Error", "Poster URL is null", exception);
                }

                try {
                    if (!e.getDescription().isEmpty()) {
                        t1.setText(e.getDescription());
                    }
                    else {
                        String error = "No description";
                        t1.setText(error);
                    }
                } catch (NullPointerException exception) {
                    Log.e("Error", "Description is null", exception);
                    String error = "No description";
                    t1.setText(error);
                }

                try {
                    if (!e.getName().isEmpty()) {
                        t2.setText(e.getName());
                    }
                    else {
                        String error = "No name";
                        t1.setText(error);
                    }
                } catch (NullPointerException exception) {
                    Log.e("Error", "Name is null", exception);
                    String error = "No name";
                    t2.setText(error);
                }
            }
        });

        binding.editButton.setOnClickListener(view1 -> {
            showEditDialog(e);
        });
    }

    private void showEditDialog(Event e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_event, null);

        // Find views in the custom layout
        EditText eventNameEditText = dialogView.findViewById(R.id.edit_event_name);
        EditText eventDescriptionEditText = dialogView.findViewById(R.id.edit_event_description);
        ImageView eventQRCodeImageView = dialogView.findViewById(R.id.edit_event_poster_imageview);
        Button uploadButton = dialogView.findViewById(R.id.upload_event_image_button);

        // Set the existing values if needed
        eventNameEditText.setText(e.getName());
        eventDescriptionEditText.setText(e.getDescription());

        builder.setView(dialogView)
                .setPositiveButton("Save", (dialog, id) -> {
                    // Handle saving the edited values
                    String newName = eventNameEditText.getText().toString();
                    String newDescription = eventDescriptionEditText.getText().toString();
                    // Update the event
                    e.setName(newName);
                    e.setDescription(newDescription);

                    Organizer o = app.getOrganizer();
                    updateEventInFirebase(o.getId(), e);

                    //((MyApplication) requireActivity().getApplication()).setOrganizerLiveData(o);
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void updateEventInFirebase(String userId, Event updatedEvent) {
        DocumentReference userDocRef = app.getDb().collection("users").document(userId);

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Get the current user
                Organizer organizer = documentSnapshot.toObject(Organizer.class);
                if (organizer != null) {
                    ArrayList<Event> events = organizer.getEvents();

                    // Find and update the specific event
                    for (int i = 0; i < events.size(); i++) {
                        if (events.get(i).getId().equals(updatedEvent.getId())) {
                            events.set(i, updatedEvent);  // Replace with the updated event
                            break;
                        }
                    }
                    // Write the updated events list back to Firebase
                    userDocRef.update("events", events)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Firebase", "Event successfully updated!");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firebase", "Error updating event", e);
                            });
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Error fetching user data", e);
        });
    }


}
