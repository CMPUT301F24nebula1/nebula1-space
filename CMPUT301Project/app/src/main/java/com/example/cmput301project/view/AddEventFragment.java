package com.example.cmput301project.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.MyApplication;
import com.example.cmput301project.controller.OrganizerEventController;
import com.example.cmput301project.R;
import com.example.cmput301project.databinding.AddEventBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

/**
 * Fragment for organizers to add an event
 * @author Xinjia Fan
 */

public class AddEventFragment extends Fragment {
    private OrganizerEventController organizerEventController;

    private AddEventBinding binding;
    private Uri imageUri;  // Store image URI after selecting it
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = AddEventBinding.inflate(inflater, container, false);

        MyApplication app = (MyApplication) requireActivity().getApplication();
        db = app.getDb();
        app.getOrganizerLiveData().observe(getViewLifecycleOwner(), organizer -> {
            if (organizer != null) {
                        // Update the UI with the organizer data
                        organizerEventController = new OrganizerEventController(organizer, db);
                    }
        });

        binding.saveEventButton.setOnClickListener(view1 -> {
            String name = binding.eventNameEdittext.getText().toString();
            String description = binding.eventDescriptionEdittext.getText().toString();

            if (!name.isEmpty()) {

                organizerEventController.addEvent(name, description, imageUri, aVoid -> {

                    NavHostFragment.findNavController(this).navigate(R.id.action_AddEvent_to_EventList);
                    NavHostFragment.findNavController(this).popBackStack(R.id.AddEventFragment, true);

                }, e -> {
                    Log.e("save event", "Error: " + e.getMessage());
                    Toast.makeText(getContext(), "Error saving event", Toast.LENGTH_SHORT).show();
                });
            } else {
                new AlertDialog.Builder(getContext())
                        .setTitle("Alert")
                        .setMessage("An event has to have a name.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();  // Close the dialog
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;
            }
        });

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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
