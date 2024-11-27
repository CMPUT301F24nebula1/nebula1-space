package com.example.cmput301project.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cmput301project.MyApplication;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.EntrantController;
import com.example.cmput301project.databinding.EntrantProfileBinding;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Organizer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OrganizerFacilityProfileFragment extends Fragment {
    // use the same layout as entrant profile
    private EntrantProfileBinding binding;
    private MyApplication app;

    private Organizer organizer;

    private TextView t_name, t_email, t_phone;
    protected Button editImageButton;
    private MaterialCardView btnEditSave;
    private ImageView imageView;
    private Uri imageUri;
    private boolean isEditMode = false;
    private boolean isImageEnlarged = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        app = (MyApplication) requireActivity().getApplication();
        binding = EntrantProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        organizer = app.getOrganizer();

        t_name = binding.entrantProfileName;
        t_email = binding.entrantProfileEmail;
        t_phone = binding.entrantProfilePhone;

        app.getOrganizerLiveData().observe(getViewLifecycleOwner(), organizer1 -> {
            organizer = organizer1;
            populateOrganizerInfo(organizer);
        });

        editImageButton = view.findViewById(R.id.edit_profile_picture_button);

        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageOptionsDialog();
                //openImagePicker(); // Call the method when the button is clicked
            }
        });
        imageView = binding.profileImageview;
        btnEditSave = binding.btnEditSave;

        setEditMode(false);

        btnEditSave.setOnClickListener(v -> {
            if (!isEditMode) {
                // Enable edit mode
                setEditMode(true);
//                btnEditSave.setText("Save");
                binding.text.setText("Save");
                binding.icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_save));
            } else {
                if (organizer != null) {
                    // Save changes and go back
                    if (t_name.getText().toString().isEmpty() || t_email.getText().toString().isEmpty()) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Alert")
                                .setMessage("Name and email can not be empty.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();  // Close the dialog
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        return;
                    }
                    else if (!validateEmail(t_email.getText().toString())) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Alert")
                                .setMessage("Invalid email form.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();  // Close the dialog
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        return;
                    }
                    saveChanges();
                    setEditMode(false);
                    binding.text.setText("Edit");
                    binding.icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_edit));
//                onBackPressed();  // Navigate back
                }
                else {
                    Log.d("save profile", "wait for loading information");
                    new AlertDialog.Builder(getContext())
                            .setTitle("Alert")
                            .setMessage("wait for loading information.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();  // Close the dialog
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return;
                }
            }
        });

        imageView.setOnClickListener(v -> toggleImageSize());

        t_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call the validation function whenever text changes
                validateEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed here
            }
        });
        app.setOrganizerLiveData(organizer);
    }

    private void populateOrganizerInfo(Organizer organizer) {
        if (organizer != null) {
            // Name field
            t_name.setText(organizer.getName());
            t_email.setText(organizer.getEmail()); // Email field
            t_phone.setText(organizer.getPhone()); // Phone field
            //binding.profileImageview.setImageDrawable(createInitialsDrawable(entrant.getName()));
            if (organizer.getProfilePictureUrl() == null || organizer.getProfilePictureUrl().isEmpty()) {
                binding.profileImageview.setImageDrawable(createInitialsDrawable(organizer.getName()));
            }
            try {
                if (!organizer.getProfilePictureUrl().isEmpty()) {
                    Glide.with(getContext())
                            .load(organizer.getProfilePictureUrl())
                            .placeholder(R.drawable.placeholder_image)  // placeholder
                            .error(R.drawable.error_image)              // error image
                            .into(binding.profileImageview);
                }
            } catch (NullPointerException e) {
                Log.e("profile picture", "No picture");
            }

        }
    }

    private BitmapDrawable createInitialsDrawable(String name) {

        String initials = getInitials(name);
        int size = 150;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(50f);
        canvas.drawText(initials, size / 2, size / 2 + 15, paint);

        return new BitmapDrawable(getResources(), bitmap);
    }

    private String getInitials(String name) {
        if (TextUtils.isEmpty(name)) return "";
        String[] parts = name.trim().split(" ");
        String initials = "";
        for (String part : parts) {
            if (!TextUtils.isEmpty(part)) {
                initials += part.charAt(0);
            }
        }
        return initials.toUpperCase();
    }

    private boolean validateEmail(String email) {
        // Use Android's Patterns utility class to check if the email format is valid
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            t_email.setError(null); // Clear error if email is valid
            return true;
        } else {
            t_email.setError("Invalid email format");
            return false;
        }
    }

    private void toggleImageSize() {
        if (isImageEnlarged) {
            // Shrink back to original size
            ScaleAnimation shrinkAnimation = new ScaleAnimation(
                    2.0f, 1.0f,   // Start and end values for the X axis scaling
                    2.0f, 1.0f,   // Start and end values for the Y axis scaling
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
            shrinkAnimation.setDuration(300);
            shrinkAnimation.setFillAfter(true); // Keeps the result of the animation
            imageView.startAnimation(shrinkAnimation);
        } else {
            // Enlarge the image
            ScaleAnimation enlargeAnimation = new ScaleAnimation(
                    1.0f, 2.0f,   // Start and end values for the X axis scaling
                    1.0f, 2.0f,   // Start and end values for the Y axis scaling
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
            enlargeAnimation.setDuration(300);
            enlargeAnimation.setFillAfter(true); // Keeps the result of the animation
            imageView.startAnimation(enlargeAnimation);
        }
        // Toggle the boolean flag
        isImageEnlarged = !isImageEnlarged;
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
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private void removeProfilePicture() {
        imageUri = null; // Clear the image URI
        imageView.setImageDrawable(createInitialsDrawable(organizer.getName())); // Reset to initials
        organizer.setProfilePictureUrl(null); // Remove URL from entrant
    }

    private void setEditMode(boolean enabled) {
        isEditMode = enabled;
        t_name.setEnabled(enabled);
        t_name.setFocusable(enabled);
        t_name.setFocusableInTouchMode(enabled);
        if (enabled) {
            t_name.requestFocus(); // Request focus for EditText
//            /showKeyboard(editText) // Show the keyboard
            binding.rfiks2zoyc1.setBackground(getResources().getDrawable(R.drawable.s79747esw1cr4));
            binding.ruyuoa2jj66p.setBackground(getResources().getDrawable(R.drawable.s79747esw1cr4));
            binding.rntsn8cfg1cd.setBackground(getResources().getDrawable(R.drawable.s79747esw1cr4));
            t_name.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF000000")));
            t_email.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF000000")));
            t_phone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF000000")));
            editImageButton.setVisibility(View.VISIBLE);
        } else {
            // clear focus when disabling edit mode
            t_name.clearFocus();
            binding.rfiks2zoyc1.setBackground(getResources().getDrawable(R.drawable.grey_border));
            binding.ruyuoa2jj66p.setBackground(getResources().getDrawable(R.drawable.grey_border));
            binding.rntsn8cfg1cd.setBackground(getResources().getDrawable(R.drawable.grey_border));
            t_name.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00FFFFFF")));
            t_email.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00FFFFFF")));
            t_phone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00FFFFFF")));
            editImageButton.setVisibility(View.INVISIBLE);
        }
        t_email.setEnabled(enabled);
        t_email.setFocusable(enabled);
        t_email.setFocusableInTouchMode(enabled);
        t_phone.setEnabled(enabled);
        t_phone.setFocusable(enabled);
        t_phone.setFocusableInTouchMode(enabled);
        editImageButton.setEnabled(enabled);
    }

    private void saveChanges() {
        // Implement the logic to save the changes
        organizer.setName(t_name.getText().toString());
        organizer.setEmail(t_email.getText().toString());
        organizer.setPhone(t_phone.getText().toString());

        saveOrganizerToDatabase(organizer, imageUri);
        app.setOrganizer(organizer); // Save data to the application variable
    }

    public void saveOrganizerToDatabase(Organizer organizer, Uri u) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> organizerData = new HashMap<>();
        organizerData.put("name", organizer.getName());
        organizerData.put("email", organizer.getEmail());
        organizerData.put("phone", organizer.getPhone());
        organizerData.put("profilePictureUrl", organizer.getProfilePictureUrl());

        lockUI();
        if (u != null) {
            uploadImageToFirebase(u, new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String downloadUrl) {
                    Log.e("upload profile image", "success");
                    organizer.setProfilePictureUrl(downloadUrl);
                    organizerData.put("profilePictureUrl", downloadUrl);
                    db.collection("organizers")
                            .document(organizer.getId())
                            .update(organizerData)
                            .addOnSuccessListener(aVoid -> {
                                unlockUI();
                                Log.d("saveEntrantToDatabase", "Entrant data updated successfully in Firebase");
                            })
                            .addOnFailureListener(e -> {
                                unlockUI();
                                Log.e("saveEntrantToDatabase", "Failed to update entrant data in Firebase", e);
                            });
                }
            }, e -> {
                unlockUI();
                Log.e("upload profile image", "failure uploading profile image");
            });
        }
        else if (organizer.getProfilePictureUrl() == null){
            imageView.setImageDrawable(createInitialsDrawable(organizer.getName()));
            db.collection("organizers")
                    .document(organizer.getId())
                    .update(organizerData)
                    .addOnSuccessListener(aVoid -> {
                        unlockUI();
                        Log.d("saveEntrantToDatabase", "Entrant data updated successfully in Firebase");
                    })
                    .addOnFailureListener(e -> {
                        unlockUI();
                        Log.e("saveEntrantToDatabase", "Failed to update entrant data in Firebase", e);
                    });

        }
        else {
            db.collection("organizers")
                    .document(organizer.getId())
                    .update(organizerData)
                    .addOnSuccessListener(aVoid -> {
                        unlockUI();
                        Log.d("saveEntrantToDatabase", "Entrant data updated successfully in Firebase");
                    })
                    .addOnFailureListener(e -> {
                        unlockUI();
                        Log.e("saveEntrantToDatabase", "Failed to update entrant data in Firebase", e);
                    });
        }
        Log.d("save entrant profile", organizer.toString());
    }

    public void uploadImageToFirebase(Uri imageUri, OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images/" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String downloadUrl = downloadUri.toString();
                            Log.d("uploadImageToFirebase", "Image URI: " + imageUri.toString());
                            successListener.onSuccess(downloadUrl);  // Pass the string URL
                        }))
                .addOnFailureListener(failureListener);

    }

    private void showImageOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Profile Picture");

        String[] options = {"Select Picture", "Remove Picture"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Select Picture
                        openImagePicker();
                        break;
                    case 1:
                        removeProfilePicture();
                        break;
                }
            }
        });

        builder.show();
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
