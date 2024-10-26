package com.example.cmput301project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.databinding.EntrantProfileBinding;

import java.io.IOException;

public class EntrantProfileFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private EntrantProfileBinding binding;
    MyApplication app;
    private Entrant entrant;
    private TextView t_name, t_email, t_phone;
    protected Button editImageButton;
    private ImageView imageView;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

//        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.entrant_profile, container, false);
        app = (MyApplication) requireActivity().getApplication();
        binding = EntrantProfileBinding.inflate(inflater, container, false);
//        View view = binding.getRoot();

        // Access your application instance
//        MyApplication myApp = (MyApplication) getActivity().getApplication();


//        binding.editProfilePictureButton.setOnClickListener(v -> captureProfilePicture());
        return binding.getRoot();
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
    private void populateEntrantInfo() {
        if (entrant != null) {
//            binding.rcmftz4npudn.setText(entrant.getName()); // Name field
//            binding.rpplfbkhl9ui.setText(entrant.getEmail()); // Email field
//            binding.ruob2m4jmcmj.setText(entrant.getPhone()); // Phone field

            // Name field
            t_name.setText(entrant.getName());
            t_email.setText(entrant.getEmail()); // Email field
            t_phone.setText(entrant.getPhone()); // Phone field
//            if (entrant.getProfilePicture() != null) {
//                binding.eventImageview.setImageBitmap(entrant.getProfilePicture());
//            } else {
            binding.profileImageview.setImageDrawable(createInitialsDrawable(entrant.getName()));
//            }
        }
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
        if (TextUtils.isEmpty(name)) return "JD";
        String[] parts = name.trim().split(" ");
        String initials = "";
        for (String part : parts) {
            if (!TextUtils.isEmpty(part)) {
                initials += part.charAt(0);
            }
        }
        return initials.toUpperCase();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        Entrant entrant = app.getEntrant();
// Now you can use myApp to access methods or properties from your MyApplication class
        // For example: myApp.someMethod();
        entrant = app.getEntrant();
        t_name = binding.entrantProfileName;
        t_email = binding.entrantProfileEmail;
        t_phone = binding.entrantProfilePhone;

//        editImageButton = binding.editProfilePictureButton;
        editImageButton = view.findViewById(R.id.edit_profile_picture_button);
// Assuming you've already initialized editImageButton
        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker(); // Call the method when the button is clicked
            }
        });
        imageView = binding.profileImageview;

        populateEntrantInfo();

        app.setEntrantLiveData(entrant);
    }
}
