package com.example.cmput301project.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import com.example.cmput301project.databinding.OrganizerEventViewBinding;
import com.example.cmput301project.view.OrganizerEventDetailFragmentArgs;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.OrganizerEventController;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

/**
 * Fragment for organizers to view their event details.
 * @author Xinjia Fan
 */
public class OrganizerEventDetailFragment extends Fragment {
    private OrganizerEventViewBinding binding;
    //OrganizerEventDetailBinding binding;
    private FirebaseFirestore db;
    private Event e;
    private ImageView posterImageview;
    private Uri imageUri;
    private MyApplication app;
    private OrganizerEventController ec;
    Organizer o;

    private Calendar startDate, endDate;
    Boolean isEditMode = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = OrganizerEventViewBinding.inflate(inflater, container, false);
        if (getArguments() != null) {
            e = OrganizerEventDetailFragmentArgs.fromBundle(getArguments()).getE();
        }
        //binding = OrganizerEventDetailBinding.inflate(inflater, container, false);

        binding.qrLayout.setVisibility(View.VISIBLE);

        app = (MyApplication) requireActivity().getApplication();
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        Organizer o = app.getOrganizer();
        db = FirebaseFirestore.getInstance();
        ec = new OrganizerEventController(o, db);

        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();

        posterImageview = binding.eventImageview;
        ImageView qrImageview = binding.eventQrcodeImageview;
        TextInputLayout t1 = binding.eventDescription;
        TextInputLayout t2 = binding.eventName;
        TextInputLayout startDate = binding.lotteryStartsDate;
        TextInputLayout endDate = binding.lotteryEndsDate;
        TextInputLayout limit = binding.lotteryCapacity;
        TextInputEditText qr = binding.qrCodeEdittext;

        qr.setEnabled(true);
        qrImageview.setVisibility(View.VISIBLE);
        binding.saveEventButton.setVisibility(View.VISIBLE);
        setButtonDisabled();

        o = app.getOrganizerLiveData().getValue();
        ec = new OrganizerEventController(o, app.getFb());

        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();

        posterImageview = binding.eventImageview;
        ImageView qrImageview = binding.eventQrcodeImageview;
        TextInputLayout t1 = binding.eventDescription;
        TextInputLayout t2 = binding.eventName;
        TextInputLayout startDate = binding.lotteryStartsDate;
        TextInputLayout endDate = binding.lotteryEndsDate;
        TextInputLayout limit = binding.lotteryCapacity;
        TextInputEditText qr = binding.qrCodeEdittext;

        qr.setEnabled(true);
        qrImageview.setVisibility(View.VISIBLE);
        binding.saveEventButton.setVisibility(View.VISIBLE);

        app.getOrganizerLiveData().observe(getViewLifecycleOwner(), organizer -> {
            // Use the organizer data here
            if (organizer != null) {
                // Use the organizer data (e.g., set organizer-related data in the UI)
                Log.d("Organizer", "Organizer Name: " + organizer.getName());

                Glide.with(getContext())
                        .load(e.getQrCode())
                        .placeholder(R.drawable.placeholder_image)  // placeholder
                        .error(R.drawable.error_image)              // error image
                        .into(qrImageview);

                try {
                    if (!e.getPosterUrl().isEmpty()) {
                        binding.eventImageview.setVisibility(View.VISIBLE);
                        Glide.with(getContext())
                                .load(e.getPosterUrl())
                                .placeholder(R.drawable.placeholder_image)  // placeholder
                                .error(R.drawable.error_image)              // error image
                                .into(posterImageview);
                    }
                    else {
                        binding.eventImageview.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.d("event poster", "poster url is null");
                }


                t1.getEditText().setText(e.getDescription());
                t2.getEditText().setText(e.getName());
                startDate.getEditText().setText(e.getStartDate());
                endDate.getEditText().setText(e.getEndDate());
                limit.getEditText().setText(e.getLimit() != 0 ? String.valueOf(e.getLimit()) : "");

            }
        });

        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.eventQrcodeImageview.getVisibility() == View.VISIBLE) {
                    binding.eventQrcodeImageview.setVisibility(View.GONE);
                }
                else {
                    binding.eventQrcodeImageview.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.saveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEditMode) {
                    setButtonsEnabled();
                    isEditMode = false;
                    binding.text.setText("Save");
                    binding.icon.setImageResource(R.drawable.ic_save);
                }
                else {
                    e.setName(t2.getEditText().getText().toString());
                    e.setDescription(t1.getEditText().getText().toString());
                    e.setStartDate(startDate.getEditText().getText().toString());
                    e.setEndDate(startDate.getEditText().getText().toString());
//                    if (limit.getEditText().toString().isEmpty()) {
//                        e.setLimit(0);
//                    } else {
//                        e.setLimit(Integer.parseInt(limit.getEditText().toString()));
//                    }
//                    for (int i = 0; i < o.getEvents().size(); i++) {
//                        if (e.getId().equals(o.getEvents().get(i).getId())) {
//                            o.getEvents().remove(o.getEvents().get(i));
//                            o.getEvents().add(e);
//                            break;
//                        }
//                    }
                    ec.editEvent(e, imageUri, aVoid -> {
                        Log.d("Firebase", "Event edited successfully");
                    }, f -> {
                        Log.d("Firebase", "Fails.");
                    });
                    for (int i = 0; i < o.getEvents().size(); i++) {
                        if (e.getId().equals(o.getEvents().get(i).getId())) {
                            o.getEvents().remove(o.getEvents().get(i));
                            o.getEvents().add(e);
                            break;
                        }
                    }

                    //app.setOrganizerLiveData(o);
                    setButtonDisabled();

                    isEditMode = true;
                    binding.text.setText("Edit");
                    binding.icon.setImageResource(R.drawable.ic_edit);
                }
            }
        });

        binding.startDateText.setOnClickListener(v -> {
            Log.d("DatePicker", "Start Date Clicked");
            showDatePickerDialog(true);
        });
        binding.endDateText.setOnClickListener(v -> {
            Log.d("DatePicker", "End Date Clicked");
            showDatePickerDialog(false);
        });

        binding.selectImageButton.setOnClickListener(view12 -> openImagePicker());
    }

    public void setButtonsEnabled() {
        binding.eventName.setEnabled(true);
        binding.eventDescription.setEnabled(true);
        binding.startDateText.setEnabled(true);
        binding.endDateText.setEnabled(true);
        binding.lotteryCapacity.setEnabled(true);
        binding.posterButton.setEnabled(true);
        binding.selectImageButton.setEnabled(true);
    }

    public void setButtonDisabled() {
        binding.eventName.setEnabled(false);
        binding.eventDescription.setEnabled(false);
        binding.startDateText.setEnabled(false);
        binding.endDateText.setEnabled(false);
        binding.lotteryCapacity.setEnabled(false);
        binding.posterButton.setEnabled(false);
        binding.selectImageButton.setEnabled(false);

    }

    private void showDatePickerDialog(final boolean isStartDate) {
        Calendar calendar = isStartDate ? startDate : endDate;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    updateDateText(isStartDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        if (!isStartDate && startDate != null) {
            // Set minimum date for endDate picker as the selected start date
            datePickerDialog.getDatePicker().setMinDate(startDate.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    private void updateDateText(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDate : endDate;
        String dateText = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR));

        dateText = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.YEAR);

        if (isStartDate) {
            binding.startDateText.setText(dateText);
        } else {
            binding.endDateText.setText(dateText);
        }
    }

    private void updateDateText(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDate : endDate;
        String dateText = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR));

//        String dateText = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
//                (calendar.get(Calendar.MONTH) + 1) + "/" +
//                calendar.get(Calendar.YEAR);

        if (isStartDate) {
            binding.startDateText.setText(dateText);
        } else {
            binding.endDateText.setText(dateText);
        }
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
//                    if (!newName.isEmpty()) {
//                        e.setName(newName);
//                    }
//
//                    e.setDescription(newDescription);
//
//                    if (imageUri != null) {
//                        app.uploadImageAndSetEvent(imageUri, e);
//                    }
//
//                    app.setOrganizerLiveData(o);
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
                        posterImageview.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.qrLayout.setVisibility(View.GONE);
    }
}
