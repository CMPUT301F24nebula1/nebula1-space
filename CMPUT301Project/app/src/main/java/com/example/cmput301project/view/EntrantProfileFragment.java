package com.example.cmput301project.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cmput301project.MyApplication;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.EntrantController;
import com.example.cmput301project.databinding.EntrantProfileBinding;
import com.example.cmput301project.model.Entrant;

import java.io.IOException;

/**
 * Fragment for entrant profile
 * @author Xinjia Fan
 */
public class EntrantProfileFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private EntrantProfileBinding binding;
    private MyApplication app;
    private Entrant entrant;
    private EntrantController ec;
    private TextView t_name, t_email, t_phone;
    protected Button editImageButton, btnEditSave;
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
        entrant = app.getEntrant();
        ec = new EntrantController(app.getEntrant());

        t_name = binding.entrantProfileName;
        t_email = binding.entrantProfileEmail;
        t_phone = binding.entrantProfilePhone;

        app.getEntrantLiveData().observe(getViewLifecycleOwner(), entrant1 -> {
            entrant = entrant1;
            populateEntrantInfo(entrant1);
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
                btnEditSave.setText("Save");
            } else {
                if (entrant != null) {
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
                    btnEditSave.setText("Edit");
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
        app.setEntrantLiveData(entrant);
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
    private void populateEntrantInfo(Entrant entrant) {
        if (entrant != null) {
            // Name field
            t_name.setText(entrant.getName());
            t_email.setText(entrant.getEmail()); // Email field
            t_phone.setText(entrant.getPhone()); // Phone field
            binding.profileImageview.setImageDrawable(createInitialsDrawable(entrant.getName()));
            try {
                if (!entrant.getProfilePictureUrl().isEmpty()) {
                    Glide.with(getContext())
                            .load(entrant.getProfilePictureUrl())
                            .placeholder(R.drawable.placeholder_image)  // placeholder
                            .error(R.drawable.error_image)              // error image
                            .into(binding.profileImageview);
                }
            } catch (NullPointerException e) {
                Log.e("profile picture", "No picture");
            }

        }
    }

    private void removeProfilePicture() {
        imageUri = null; // Clear the image URI
        imageView.setImageDrawable(createInitialsDrawable(entrant.getName())); // Reset to initials
        entrant.setProfilePictureUrl(null); // Remove URL from entrant
        ec.saveEntrantToDatabase(entrant, null); // Save the change to the database
    }

    private void captureProfilePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
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

    private void setEditMode(boolean enabled) {
        isEditMode = enabled;
        t_name.setEnabled(enabled);
        t_name.setFocusable(enabled);
        t_name.setFocusableInTouchMode(enabled);
        if (enabled) {
            t_name.requestFocus(); // Request focus for EditText
//            /showKeyboard(editText) // Show the keyboard
        } else {
            // Optionally, clear focus when disabling edit mode
            t_name.clearFocus();
        }
        t_email.setEnabled(enabled);
        t_email.setFocusable(enabled);
        t_email.setFocusableInTouchMode(enabled);
        t_phone.setEnabled(enabled);
        t_phone.setFocusable(enabled);
        t_phone.setFocusableInTouchMode(enabled);
        editImageButton.setEnabled(enabled);
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void onBackPressed() {
        if (isEditMode) {
            // Exit edit mode without navigating back
            setEditMode(false);
        } else {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }
    private void saveChanges() {
        // Implement the logic to save the changes
        entrant.setName(t_name.getText().toString());
        entrant.setEmail(t_email.getText().toString());
        entrant.setPhone(t_phone.getText().toString());
        // Save data1 and data2 to database or shared preferences
        ec.saveEntrantToDatabase(entrant, imageUri);
        app.setEntrantLiveData(entrant); // Save data to the application variable
    }
}
