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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddEventFragment extends Fragment {
    private AddEventBinding binding;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView eventImageView;

    private SharedViewModel sharedViewModel;
    private FirebaseFirestore db;
    private DocumentReference ref;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        binding = AddEventBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        db = sharedViewModel.getDb();
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        eventImageView = binding.eventImageview;

        Organizer o = sharedViewModel.getOrganizer();
        ref = db.collection("users").document(o.getId());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                // Use ImageDecoder for API level 28 and above
                                Bitmap decodedBitmap = null;
                                if (imageUri != null) {
                                    decodedBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                                }
                                eventImageView.setImageBitmap(decodedBitmap);
                            } else {
                                // For older APIs, use MediaStore
                                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                                eventImageView.setImageBitmap(bitmap);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        binding.selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });

        binding.saveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // An event has to have a name
                if (binding.eventNameEdittext.getText().toString().isEmpty()) {
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

                }
                String name = binding.eventNameEdittext.getText().toString();
                String description = binding.eventDescriptionEdittext.getText().toString();
                //String posterUrl = convertImageViewToBase64String(eventImageView);
                Event event = new Event();
                event.setName(name);
                //event.setPosterUrl(posterUrl);
                event.setDescription(description);
                o.create_event(event);
                addEventToUser(o.getId(), event);

                NavHostFragment.findNavController(AddEventFragment.this)
                        .navigate(R.id.action_AddEvent_to_EventList);
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    public String convertImageViewToBase64String(ImageView imageView) {
        // Step 1: Get the Bitmap from the ImageView
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();

        // Step 2: Convert the Bitmap to a ByteArrayOutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);  // Compress as PNG or JPEG
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Step 3: Encode the byte array into a Base64 string
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return encodedImage;  // Return the Base64 encoded string
    }

    // when organizer adds an event, upload it to the firebase
    public void addEventToUser(String userId, Event newEvent) {
        // Reference to the user's document
        DocumentReference userDocRef = db.collection("users").document(userId);

        // Use the Firestore update() method to update the events array field
        userDocRef.update("events", FieldValue.arrayUnion(newEvent))
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Event successfully added!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error updating events", e);
                });
    }

}
