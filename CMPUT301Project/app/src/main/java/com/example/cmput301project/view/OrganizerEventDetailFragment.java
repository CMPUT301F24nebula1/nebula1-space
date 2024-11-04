package com.example.cmput301project.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cmput301project.MyApplication;
import com.example.cmput301project.view.OrganizerEventDetailFragmentArgs;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.OrganizerEventController;
import com.example.cmput301project.databinding.OrganizerEventDetailBinding;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

/**
 * Fragment for organizers to view their event details.
 * @author Xinjia Fan
 */
public class OrganizerEventDetailFragment extends Fragment {
    OrganizerEventDetailBinding binding;
    private FirebaseFirestore db;
    private Event e;
    private Uri imageUri;
    private MyApplication app;
    private ImageView eventPosterImageView;
    private OrganizerEventController ec;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = OrganizerEventDetailBinding.inflate(inflater, container, false);
        e = OrganizerEventDetailFragmentArgs.fromBundle(getArguments()).getE();
        app = (MyApplication) requireActivity().getApplication();
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        Organizer o = app.getOrganizerLiveData().getValue();
//        db = FirebaseFirestore.getInstance();
        ec = new OrganizerEventController(o, app.getFb());




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
                    Log.e("Error", "QR code is null", exception);
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

//        binding.editButton.setOnClickListener(view1 -> {
//            showEditDialog(e);
//        });
    }

//    private void showEditDialog(Event e) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        LayoutInflater inflater = getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.dialog_edit_event, null);
//
//        // Find views in the custom layout
//        EditText eventNameEditText = dialogView.findViewById(R.id.edit_event_name);
//        EditText eventDescriptionEditText = dialogView.findViewById(R.id.edit_event_description);
//        eventPosterImageView = dialogView.findViewById(R.id.edit_event_poster_imageview);
//        Button uploadButton = dialogView.findViewById(R.id.upload_event_image_button);
//
//        // Set the existing values if needed
//        eventNameEditText.setText(e.getName());
//        eventDescriptionEditText.setText(e.getDescription());
//
//        try {
//            if (!e.getPosterUrl().isEmpty()) {
//                Glide.with(getContext())
//                        .load(e.getPosterUrl())
//                        .placeholder(R.drawable.placeholder_image)  // placeholder
//                        .error(R.drawable.error_image)              // error image
//                        .into(eventPosterImageView);
//            }
//        } catch (NullPointerException exception) {
//            Log.e("Error", "Poster URL is null", exception);
//        }
//
//        uploadButton.setOnClickListener(view -> openImagePicker());
//
//        builder.setView(dialogView)
//                .setPositiveButton("Save", (dialog, id) -> {
//                    // Handle saving the edited values
//                    String newName = eventNameEditText.getText().toString();
//                    String newDescription = eventDescriptionEditText.getText().toString();
//
//                    ec.editEvent(e, newName, newDescription, imageUri, aVoid -> {
//                        Log.d("Firebase", "Event edited successfully");
//                    }, f -> {
//                        Log.d("Firebase", "Fails.");
//                    });
//                })
//                .setNegativeButton("Cancel", (dialog, id) -> {
//                    dialog.dismiss();
//                });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        eventPosterImageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );
}
