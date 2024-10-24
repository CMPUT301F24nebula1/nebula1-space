package com.example.cmput301project;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.databinding.AddEventBinding;
import com.example.cmput301project.databinding.OrganizerEventListBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddEventFragment extends Fragment {
    private AddEventController addEventController;
    private AddEventBinding binding;
    private Uri imageUri;  // Store image URI after selecting it
    private FirebaseFirestore db;

    private SharedViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        binding = AddEventBinding.inflate(inflater, container, false);
        Organizer organizer = sharedViewModel.getOrganizer();  // Retrieve organizer somehow (via arguments, singleton, etc.)
        db = FirebaseFirestore.getInstance();  // or get from sharedViewModel if needed

        // Initialize the controller
        addEventController = new AddEventController(organizer, db);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.saveEventButton.setOnClickListener(view1 -> {
            String name = binding.eventNameEdittext.getText().toString();
            String description = binding.eventDescriptionEdittext.getText().toString();

            if (!name.isEmpty()) {
                // Delegate the business logic to the controller
                addEventController.addEvent(name, description, imageUri, aVoid -> {
                    NavHostFragment.findNavController(this).navigate(R.id.action_AddEvent_to_EventList);
                    NavHostFragment.findNavController(this).popBackStack(R.id.AddEventFragment, true);
                }, e -> {
                    Toast.makeText(getContext(), "Error saving event", Toast.LENGTH_SHORT).show();
                });
            } else {
                new AlertDialog.Builder(getContext())  // 'this' refers to the current context, can be 'getContext()' if inside a Fragment
                        .setTitle("Alert")  // Set the title of the dialog
                        .setMessage("An event has to have a name.")  // Set the message for the dialog
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Action to take when the user presses the "OK" button
                                dialog.dismiss();  // Close the dialog
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)  // Optional: Set an icon
                        .show();  // Display the dialog
                return;
            }
        });

        binding.selectImageButton.setOnClickListener(view12 -> openImagePicker());
    }

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
                        binding.eventImageview.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );
}
