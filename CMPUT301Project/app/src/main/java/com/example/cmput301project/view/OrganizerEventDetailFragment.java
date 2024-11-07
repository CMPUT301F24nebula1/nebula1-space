package com.example.cmput301project.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
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
    private ImageView eventPosterImageView;
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
        binding.listButton.setVisibility(View.VISIBLE);
        setButtonDisabled();

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

        binding.listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ParticipantListActivity.class);
                intent.putExtra("event", e);
                startActivity(intent);
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

        EditText positiveIntegerEditText = binding.lotteryCapacityText;

        positiveIntegerEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        // Validate when input focus changes
        positiveIntegerEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String input = positiveIntegerEditText.getText().toString();
                if (!isValidPositiveInteger(input)) {
                    positiveIntegerEditText.setError("Please enter a number greater than zero.");
                }
            }
        });
    }

    public void setButtonsEnabled() {
        binding.eventName.setEnabled(true);
        binding.eventDescription.setEnabled(true);
        binding.lotteryStartsDate.setEnabled(true);
        binding.lotteryEndsDate.setEnabled(true);
        binding.lotteryCapacity.setEnabled(true);
        binding.posterGroup.setEnabled(true);

        binding.lotteryEndsDate.setEndIconVisible(true);
        binding.lotteryStartsDate.setEndIconVisible(true);
        binding.posterGroup.setEndIconVisible(true);
        binding.capacityNote.setVisibility(View.VISIBLE);
    }

    public void setButtonDisabled() {
        binding.eventName.setEnabled(false);
        binding.eventDescription.setEnabled(false);
        binding.lotteryStartsDate.setEnabled(false);
        binding.lotteryEndsDate.setEnabled(false);
        binding.lotteryCapacity.setEnabled(false);
        binding.posterGroup.setEnabled(false);

        binding.lotteryEndsDate.setEndIconVisible(false);
        binding.lotteryStartsDate.setEndIconVisible(false);
        binding.posterGroup.setEndIconVisible(false);
        binding.capacityNote.setVisibility(View.GONE);
    }

    // Method to validate if the input is a positive integer
    private boolean isValidPositiveInteger(String input) {
        try {
            int value = Integer.parseInt(input);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
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
