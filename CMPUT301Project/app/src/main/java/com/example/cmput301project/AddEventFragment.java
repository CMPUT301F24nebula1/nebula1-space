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
    private AddEventBinding binding;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView eventImageView;
    private Uri imageUri;

    private SharedViewModel sharedViewModel;
    private FirebaseFirestore db;
    private DocumentReference ref;
    private FirebaseFirestore firestore;

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
        firestore = FirebaseFirestore.getInstance();
        ref = db.collection("users").document(o.getId());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
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
                    return;
                }
                String name = binding.eventNameEdittext.getText().toString();
                String description = binding.eventDescriptionEdittext.getText().toString();
                //String posterUrl = convertImageViewToBase64String(eventImageView);
                Event event = new Event();
                event.setName(name);
                event.setDescription(description);
                o.create_event(event);
                if (imageUri != null) {
                    uploadImageToFirebase(imageUri);  // Call the upload function
                    storeImageUrlInFirestore(imageUri.toString());
                    event.setPosterUrl(imageUri.toString());
                }
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

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images/" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                Log.d("Firebase", "Upload successful! URL: " + downloadUrl);
                Toast.makeText(getActivity(), "Upload successful!", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Upload failed: " + e.getMessage());
            Toast.makeText(getActivity(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void storeImageUrlInFirestore(String downloadUrl) {
        // Create a map to store the image URL and other data
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("imageUrl", downloadUrl);
        imageData.put("timestamp", System.currentTimeMillis());  // Example: Adding a timestamp

        // Store the data in a collection named "images"
        firestore.collection("images")
                .add(imageData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getActivity(), "Image URL saved to Firestore!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to save image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
