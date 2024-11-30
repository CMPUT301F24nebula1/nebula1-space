package com.example.cmput301project.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.MyApplication;
import com.example.cmput301project.controller.OrganizerEventController;
import com.example.cmput301project.R;
import com.example.cmput301project.databinding.OrganizerEventViewBinding;
import com.example.cmput301project.model.Event;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

/**
 * Fragment for organizers to add an event
 *
 * @author Xinjia Fan
 */

public class AddEventFragment extends Fragment {
    public OrganizerEventViewBinding binding;
    private OrganizerEventController organizerEventController;
    private Uri imageUri;  // Store image URI after selecting it
    private FirebaseFirestore db;
    private TextView startDateText, endDateText;
    private Calendar startDate, endDate;    // haven't added this to firebase

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = OrganizerEventViewBinding.inflate(inflater, container, false);

        binding.listButton.setVisibility(View.GONE);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.selectImageButton.setOnClickListener(view12 -> openImagePicker());
        setButtonsEnabled();

        binding.saveEventButton.setVisibility(View.VISIBLE);
        binding.icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_save));
        binding.text.setText("Save");

        MyApplication app = (MyApplication) requireActivity().getApplication();

        setButtonsEnabled();

        db = app.getDb();
        app.getOrganizerLiveData().observe(getViewLifecycleOwner(), organizer -> {
            if (organizer != null) {
                // Update the UI with the organizer data
                organizerEventController = new OrganizerEventController(organizer, db);
            }
        });

        binding.saveEventButton.setOnClickListener(view1 -> {
            String name = (binding.eventName.getEditText() != null) ? binding.eventName.getEditText().getText().toString() : "";
            String description = (binding.eventDescription.getEditText() != null) ? binding.eventDescription.getEditText().getText().toString() : "";
            // check if date is ok
            String pattern = "^\\d{2}/\\d{2}/\\d{4}$";

            if (!name.isEmpty() &&
                    startDateText.getText().toString().matches(pattern) &&
                    endDateText.getText().toString().matches(pattern)) {
                if (!containsAlphabeticCharacter(name)) {
                    Toast.makeText(getContext(), "Event name must include alphabetical characters.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidPositiveInteger(Objects.requireNonNull(binding.lotteryCapacity.getEditText()).getText().toString())) {
                    Toast.makeText(getContext(), "Capacity must be greater or equal to 0.\n0 means unlimited.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Event event = new Event();
                event.setName(name);
                event.setStartDate(startDateText.getText().toString());
                event.setEndDate(endDateText.getText().toString());
                event.setDescription(description);
                event.setRequiresGeolocation(binding.geolocationCheckbox.isChecked());
                Log.d("event geolocation", String.valueOf(event.isRequiresGeolocation()));
                Log.e("event geolocation", "debug1");

                String inputCapacity = binding.lotteryCapacity.getEditText().getText().toString().trim();
                int lotteryCapacity;
                try {
                    lotteryCapacity = Integer.parseInt(inputCapacity);
                } catch (NumberFormatException e) {
                    lotteryCapacity = 0;  // Or any default value or error handling logic
                    // Optionally, you can show an error message to the user
                    binding.lotteryCapacity.setError("Please enter a valid number");
                }
                event.setLimit(lotteryCapacity);

                lockUI();
                organizerEventController.addEvent(event, imageUri, aVoid -> {
                    unlockUI();
                    Log.d("nav", "navigate to event detail");
                    NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(R.id.AddEventFragment, true)
                            .build();
                    AddEventFragmentDirections.ActionAddEventToEventDetail action = AddEventFragmentDirections.actionAddEventToEventDetail(event);
                    NavHostFragment.findNavController(AddEventFragment.this).navigate(action, navOptions);

                }, e -> {
                    unlockUI();
                    Log.e("save event", "Error: " + e.getMessage());
                    Toast.makeText(getContext(), "Error saving event", Toast.LENGTH_SHORT).show();
                });
            } else {
                new AlertDialog.Builder(getContext())
                        .setTitle("Alert")
                        .setMessage("An event has to have a name, start date and end date.")
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

        // Find views and add click listeners
        startDateText = binding.startDateText;
        endDateText = binding.endDateText;

        if (startDateText == null || endDateText == null) {
            Log.e("DatePicker", "startDateText or endDateText is null, check your layout ID"); // Log if null
//            return view;
        }

        // Initialize Calendar instances
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();

        startDateText.setOnClickListener(v -> {
            Log.d("DatePicker", "Start Date Clicked");
            showDatePickerDialog(true);
        });
        endDateText.setOnClickListener(v -> {
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
        binding.posterGroup.setEnabled(true);
        binding.selectImageButton.setEnabled(true);
        binding.capacityNote.setVisibility(View.VISIBLE);
    }

    public boolean containsAlphabeticCharacter(String str) {
        return str != null && str.matches(".*[a-zA-Z].*");
    }

    // Method to validate if the input is a positive integer
    private boolean isValidPositiveInteger(String input) {
        try {
            int value = Integer.parseInt(input);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    public ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        binding.eventImageview.setImageBitmap(bitmap);
                        binding.eventImageview.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private void showDatePickerDialog(final boolean isStartDate) {
        Calendar calendar = isStartDate ? startDate : endDate;

        // Ensure the context is not null
        if (getActivity() == null) {
            Log.e("DatePicker", "Fragment not attached to an Activity");
            return;
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);

                    if (isStartDate) {
                        startDateText.setText(formatDate(startDate));

                        // Check if end date is earlier than start date
                        if (endDate.before(startDate) && !endDateText.getText().toString().isEmpty()) {
                            endDateText.setText("");
                            Toast.makeText(getContext(), "Please ensure the end date is after the start date.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        endDateText.setText(formatDate(endDate));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Restrict dates to today or later
        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());

        // If setting the end date, restrict it to the selected start date as the minimum date

        if (!isStartDate) {
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

//        String dateText = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
//                (calendar.get(Calendar.MONTH) + 1) + "/" +
//                calendar.get(Calendar.YEAR);

        if (isStartDate) {
            startDateText.setText(dateText);
        } else {
            endDateText.setText(dateText);
        }
    }

    private String formatDate(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        return sdf.format(date.getTime());
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