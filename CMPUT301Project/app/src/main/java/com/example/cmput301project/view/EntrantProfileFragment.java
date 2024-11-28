package com.example.cmput301project.view;

import static androidx.core.content.ContextCompat.checkSelfPermission;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
//import com.example.cmput301project.Manifest;
import com.example.cmput301project.MyApplication;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.EntrantController;
import com.example.cmput301project.controller.NotificationArrayAdapter;
import com.example.cmput301project.databinding.EntrantProfileBinding;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Notification;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    protected Button editImageButton;
    private MaterialCardView btnEditSave;
    private ImageView imageView;
    private Uri imageUri;
    private boolean isEditMode = false;
    private boolean isImageEnlarged = false;
    private boolean removeProfile;

    ArrayList<Notification> notifications;
    ListView notificationList;
    NotificationArrayAdapter notificationAdapter;

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
//        entrant = app.getEntrant();
//        ec = new EntrantController(app.getEntrant());
        entrant = app.getEntrantLiveData().getValue();
        ec = new EntrantController(entrant);

        t_name = binding.entrantProfileName;
        t_email = binding.entrantProfileEmail;
        t_phone = binding.entrantProfilePhone;

        // Custom behavior for tName
        t_name.setOnEditorActionListener((v, actionId, event) -> {
            // Prevent moving to the next EditText
            v.clearFocus();
            hideKeyboard(v);
            return true; // Indicate the action is handled
        });

        t_email.setOnEditorActionListener((v, actionId, event) -> {
            // Prevent moving to the next EditText
            v.clearFocus();
            hideKeyboard(v);
            return true; // Indicate the action is handled
        });

        t_phone.setOnEditorActionListener((v, actionId, event) -> {
            // Prevent moving to the next EditText
            v.clearFocus();
            hideKeyboard(v);
            return true; // Indicate the action is handled
        });

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
//                btnEditSave.setText("Save");
                binding.text.setText("Save");
                binding.icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_save));
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

//        app.setEntrantLiveData(entrant);
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
                        removeProfile = true;
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
                        removeProfile = false;
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
            //binding.profileImageview.setImageDrawable(createInitialsDrawable(entrant.getName()));
            if (entrant.getProfilePictureUrl() == null || entrant.getProfilePictureUrl().isEmpty()) {
                binding.profileImageview.setImageDrawable(createInitialsDrawable(entrant.getName()));
            }
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

    private Uri getImageUriFromImageView(Bitmap bitmap) {
        File file = new File(getContext().getCacheDir(), "imageview_image.png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            return FileProvider.getUriForFile(getContext(), "come.example.compute301project.fileprovider", file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void removeProfilePicture() {
        imageUri = null; // Clear the image URI
        imageView.setImageDrawable(createInitialsDrawable(entrant.getName())); // Reset to initials
//        entrant.setProfilePictureUrl(null); // Remove URL from entrant
        //ec.saveEntrantToDatabase(entrant, null); // Save the change to the database
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

//        int MyVersion = Build.VERSION.SDK_INT;
//        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
//            checkIfAlreadyhavePermission();
//        }
//        imageUri = getImageUri(this.getContext(), bitmap);

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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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

        if (removeProfile) {
            entrant.setProfilePictureUrl(null);
        }
        Log.d("profile debug", "0");
        lockUI();
        ec.saveEntrantToDatabase(entrant, imageUri, new EntrantController.SaveCallback() {
            @Override
            public void onSaveSuccess() {
//                Log.d("profile debug", "1");
                unlockUI();
                app.setEntrantLiveData(entrant); // Save data to the application variable
            }
            @Override
            public void onSaveFailure(Exception e) {
                unlockUI();
            }
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);  // Ensure the fragment has access to the options menu
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem customButton = menu.findItem(R.id.btn_notification);
        if (customButton != null) {
            customButton.setVisible(true);  // Show it in this fragment
            customButton.setEnabled(true);
        }
        // Set the custom action view
        View actionView = customButton.getActionView();
        if (actionView == null) {
            actionView = LayoutInflater.from(requireContext()).inflate(R.layout.menu_notification_badge, null);
            customButton.setActionView(actionView);
        }

        // Manage badge visibility
        View badge = actionView.findViewById(R.id.notification_badge);
        app.getEntrantLiveData().observe(getViewLifecycleOwner(), entrant1 -> {
            if (Entrant.hasUnreadNotifications(entrant1.getNotifications())) {
                if (entrant1.getReceiveNotification())
                    badge.setVisibility(View.VISIBLE);
                else
                    badge.setVisibility(View.GONE);
            } else {
                badge.setVisibility(View.GONE);
            }
        });

        // Handle click on the notification icon
        actionView.setOnClickListener(v -> {
            // Trigger the menu item click
            onOptionsItemSelected(customButton);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.btn_notification) {
            // Handle the custom button click here
            showNotificationsPopup();
//            Toast.makeText(getContext(), "Custom button clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        this.entrant = app.getEntrantLiveData().getValue();
        super.onDestroy();
    }

    private void showNotificationsPopup() {
        // Inflate the popup layout
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_notifications, null);

        // Initialize the PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, 1000); // Fixed height of 400 pixels
//        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        // Set up the ListView with notifications
        notificationList = popupView.findViewById(R.id.notification_list_view);
        Button stopNotificationsButton = popupView.findViewById(R.id.stop_notifications_button);

//        notifications = entrant.getNotifications();
        retrieveEntrantNotification(entrant, new MyApplication.NotificationCallback() {
            @Override
            public void onNotificationsRetrieved(ArrayList<Notification> notifications1) {
                ListView listView = popupView.findViewById(R.id.notification_list_view);
                if (!entrant.getReceiveNotification()) {
                    listView.setVisibility(View.GONE);
                    stopNotificationsButton.setText("Receive\nNotification");
                }
                else if (entrant.getReceiveNotification()) {
                    listView.setVisibility(View.VISIBLE);
                    stopNotificationsButton.setText("Stop\nNotification");
                }
                notifications = notifications1;
//                Log.d("notification1", String.valueOf(notifications.get(0).isRead()));
                if (notificationAdapter == null) {
                    notificationAdapter = new NotificationArrayAdapter(getContext(), notifications);
                }
                notificationList.setAdapter(notificationAdapter);
            }
            @Override
            public void onError(Exception e) {}
        });

        // Stop Notifications button
        stopNotificationsButton.setOnClickListener(v -> {
            stopNotificationsForEntrant(entrant, new FirestoreUpdateCallback() {
                @Override
                public void onSuccess() {
                    unlockUI();
                    popupWindow.dismiss();
                    if (entrant.getReceiveNotification())
                        Toast.makeText(getContext(), "Notifications enabled.", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(), "Notifications stopped.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    unlockUI();
                    Toast.makeText(getContext(), "Failed to stop Notifications.", Toast.LENGTH_SHORT).show();
                }
            });

        });

        // Show the PopupWindow at the right end under the toolbar
        View toolbar = getActivity().findViewById(R.id.toolbar);

        int xOffset = toolbar.getWidth() - popupWindow.getWidth();
        popupWindow.showAsDropDown(toolbar, xOffset, 0);

        notificationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Notification notification = notifications.get(i);
                notification.setRead(true);
                notificationAdapter.notifyDataSetChanged();
//                String eventId = notification.getEventId();
                updateNotificationStatus(notification.getId(), new FirestoreUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        popupWindow.dismiss();
                        showNotificationDetailPopup(notifications.get(i));
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to retrieve data.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    private void showNotificationDetailPopup(Notification notification) {
        Context context = getContext();
        if (context == null) {
            Log.e("PopupError", "Context is null, cannot inflate the popup layout");
            return;
        }
        View detailPopupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_notification_detail, null);

        // Initialize the PopupWindow for the detail view
        PopupWindow detailPopupWindow = new PopupWindow(detailPopupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        detailPopupWindow.setFocusable(true);
        detailPopupWindow.setBackgroundDrawable(new ColorDrawable());

        // Display message
        TextView messageTextView = detailPopupView.findViewById(R.id.notification_message);
        messageTextView.setText(notification.getMessage());

        // Set up Yes and No buttons
        Button yesButton = detailPopupView.findViewById(R.id.yes_button);
        Button noButton = detailPopupView.findViewById(R.id.no_button);

        retrieveEntrantStatus(entrant, notification.getEventId(), new StatusCallback() {
            @Override
            public void onStatusRetrieved(String status) {
                if (status.equals("SELECTED")) {
                    yesButton.setVisibility(View.VISIBLE);
                    noButton.setVisibility(View.VISIBLE);
                } else {
                    yesButton.setVisibility(View.GONE);
                    noButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to retrieve data.", Toast.LENGTH_SHORT).show();
            }
        });

        yesButton.setOnClickListener(v -> {
            updateStatus(entrant, "FINAL", notification.getEventId());
            Toast.makeText(getContext(), "Accepted the invitation!", Toast.LENGTH_SHORT).show();
            detailPopupWindow.dismiss();
        });

        noButton.setOnClickListener(v -> {
            updateStatus(entrant, "CANCELED", notification.getEventId());
            Toast.makeText(getContext(), "Declined the invitation", Toast.LENGTH_SHORT).show();
            detailPopupWindow.dismiss();
        });

        ImageButton backButton = detailPopupView.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            detailPopupWindow.dismiss();
            showNotificationsPopup(); // Show the notification list again
        });

        View toolbar = getActivity().findViewById(R.id.toolbar);

        int xOffset = toolbar.getWidth() - detailPopupView.getWidth();
        detailPopupWindow.showAsDropDown(toolbar, xOffset, 0);
    }

    private void retrieveEntrantNotification(Entrant entrant, MyApplication.NotificationCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference notificationRef = db.collection("entrants")
                .document(entrant.getId())
                .collection("notifications");
        ArrayList<Notification> notifications = new ArrayList<>();

        notificationRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e("Firestore Error", "Error listening to notification updates", error);
                return;
            }

            if (snapshots != null) {
                notifications.clear();
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    Notification item = document.toObject(Notification.class);
                    if (item != null) {
                        item.setId(document.getId());

                        notifications.add(item);
                        Log.d("notification status", item.getId() + String.valueOf(item.isRead()));
                    }
                }
                Log.d("Notifications", "Notifications: " + notifications);

                // sort notifications according to timestamp
                notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
                entrant.setNotifications(notifications);
//                setEntrantLiveData(entrant);

                if (callback != null) {
                    callback.onNotificationsRetrieved(notifications);
                }
            }
        });
    }

    public interface StatusCallback {
        void onStatusRetrieved(String status);
        void onFailure(Exception e);
    }

    public void retrieveEntrantStatus(Entrant entrant, String eventId, StatusCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference waitlistRef = db.collection("entrants")
                .document(entrant.getId())
                .collection("entrantWaitList");

        waitlistRef.whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String status = document.getString("status");

                            // Check if status is not null and log it
                            if (status != null) {
                                Log.d("Firestore", "Status: " + status);
                                callback.onStatusRetrieved(status);
                            } else {
                                Log.d("Firestore", "Status field is null");
                                callback.onFailure(new NullPointerException("Status field is null"));
                            }
                        }
                    } else {
                        Log.d("Firestore", "No document found with the specified eventId");
                        callback.onFailure(new Exception("No document found with the specified eventId"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error finding document", e);
                });
    }

    public interface FirestoreUpdateCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void updateNotificationStatus(String id, FirestoreUpdateCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference notificationDocRef = db.collection("entrants")
                .document(entrant.getId())
                .collection("notifications")
                .document(id); // Replace "DOCUMENT_ID" with the specific document ID

        // Update the isRead field
        notificationDocRef.update("isRead", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore Update", "isRead field successfully updated!");
                    if (callback != null) {
                        callback.onSuccess(); // Notify success
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Update", "Error updating isRead field", e);
                    if (callback != null) {
                        callback.onFailure(e); // Notify failure
                    }
                });

    }

    public void updateStatus(Entrant entrant, String status, String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference notificationRef = db.collection("entrants")
                .document(entrant.getId())
                .collection("notifications");

        CollectionReference waitlistRef = db.collection("entrants")
                .document(entrant.getId())
                .collection("entrantWaitList");

        Log.d("notification eventId", eventId);
        waitlistRef.whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            DocumentReference docRef = document.getReference();
                            // Update the status field
                            docRef.update("status", status)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "Status field updated successfully");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error updating status field", e);
                                    });
                        }
                    } else {
                        Log.d("Firestore", "No document found with the specified eventId");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error finding document", e);
                });
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

    //method to stop notifications
    private void stopNotificationsForEntrant(Entrant entrant, FirestoreUpdateCallback callback) {
        // Update a flag in the database to disable notifications
        lockUI();

        Boolean flag;
        if (entrant.getReceiveNotification()) {
            flag = false;
            entrant.setReceiveNotification(false);
        } else {
            flag = true;
            entrant.setReceiveNotification(true);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("entrants")
                .document(entrant.getId())
                .update("receiveNotification", flag)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                    Log.d("StopNotifications", "Notifications disabled for entrant: " + entrant.getId());
                    unlockUI();
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("StopNotifications", "Failed to disable notifications for entrant: " + entrant.getId(), e);
                    unlockUI();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    // Helper method to hide the keyboard
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
