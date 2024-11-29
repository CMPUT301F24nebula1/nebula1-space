package com.example.cmput301project.view;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.MyApplication;
import com.example.cmput301project.R;
import com.example.cmput301project.databinding.EntrantHomepageBinding;
import com.example.cmput301project.model.Entrant;

/**
 * Fragment for entrant homepage
 * @author Xinjia Fan
 */
public class EntrantHomepageFragment extends Fragment {
    private EntrantHomepageBinding binding;
    private MyApplication app;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        binding = EntrantHomepageBinding.inflate(inflater, container, false);
        app = (MyApplication) requireActivity().getApplication();
        app.getEntrantLiveData().observe(getViewLifecycleOwner(), entrant -> {
            if (entrant.getReceiveNotification()) {
                if (entrant.getNotifications() == null)
                    return;
                if (Entrant.hasUnreadNotifications(entrant.getNotifications())) {
                    binding.notificationBadge.setVisibility(View.VISIBLE);
                } else {
                    binding.notificationBadge.setVisibility(View.GONE);
                }
            }
        });
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        updateNotificationButtonIcon();

        binding.profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Entrant entrant = app.getEntrantLiveData().getValue();
                if (entrant != null) {
                    NavHostFragment.findNavController(EntrantHomepageFragment.this)
                            .navigate(R.id.action_EntrantHomepage_to_EntrantProfile);

                    app.getEntrantLiveData().removeObservers(getViewLifecycleOwner());
                }
                app.getEntrantLiveData().observe(getViewLifecycleOwner(), entrant1 -> {
                    if (entrant1 != null) {
                        try {
                            Log.d("profileButton", entrant1.getName());
                        } catch (NullPointerException e) {
                            Log.d("profileButton", "No name");
                        }

                        NavHostFragment.findNavController(EntrantHomepageFragment.this)
                                .navigate(R.id.action_EntrantHomepage_to_EntrantProfile);

                        app.getEntrantLiveData().removeObservers(getViewLifecycleOwner());
                    } else {
                        Log.d("profileButton", "Entrant data is not ready yet");
                        // Optionally, you can show a loading indicator to the user.
                    }
                });
            }
        });
        binding.profileButton.setOnClickListener(v ->
                app.getEntrantLiveData().observe(getViewLifecycleOwner(), entrant1 -> {
                    Log.d("profileButton", "debug");
                    if (entrant1 != null) {
                        try {
                            Log.d("profileButton", entrant1.getName());
                        } catch (NullPointerException e) {
                            Log.d("profileButton", "No name");
                        }

                        NavHostFragment.findNavController(EntrantHomepageFragment.this)
                                .navigate(R.id.action_EntrantHomepage_to_EntrantProfile);

                        app.getEntrantLiveData().removeObservers(getViewLifecycleOwner());
                    } else {
                        Toast.makeText(getContext(), "Profile data is not ready yet.", Toast.LENGTH_SHORT).show();

                        Log.d("profileButton", "Entrant data is not ready yet");
                        // Optionally, you can show a loading indicator to the user.
                    }
                })
        );

        binding.scanQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissionLauncherCamera.launch(android.Manifest.permission.CAMERA);
                Intent intent = new Intent(getActivity(), ScannerActivity.class);
                startActivity(intent);
            }
        });

        binding.myClassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                app.getEntrantLiveData().observe(getViewLifecycleOwner(), entrant1 -> {
                    if (entrant1 != null) {
                        try {
                            Log.d("My Classes", entrant1.getWaitlistEventIds().toString());
//                            Toast.makeText(getContext(), "Loading!", Toast.LENGTH_SHORT).show();
                        } catch (NullPointerException e) {
                            Log.d("My Classes", "No class");
                            Toast.makeText(getContext(), "Class data is not ready yet.", Toast.LENGTH_SHORT).show();
                        }

                        NavHostFragment.findNavController(EntrantHomepageFragment.this)
                                .navigate(R.id.action_EntrantHomepage_to_EntrantClass);

//                        app.getEntrantLiveData().removeObservers(getViewLifecycleOwner());
                    } else {
                        Toast.makeText(getContext(), "Class data is not ready yet.", Toast.LENGTH_SHORT).show();

                        Log.d("My class", "Class data is not ready yet");
                    }
                });
            }
        });

        binding.btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!areNotificationsEnabled(requireContext())) {
                    showEnableNotificationsDialog();
                    updateNotificationButtonIcon();
                }
                else {
                    showDisableNotificationsDialog();
                    updateNotificationButtonIcon();
                }
            }
        });

    }

    private void updateNotificationButtonIcon() {
        if (areNotificationsEnabled(requireContext())) {
            binding.icon.setImageResource(R.drawable.ic_notification_off);
        } else {
            binding.icon.setImageResource(R.drawable.ic_notification);
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncherCamera =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. You can now use the camera.
                } else {
                    // Permission denied. Inform the user that the feature is unavailable.
                    Toast.makeText(getContext(), "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show();
                }
            });

    public boolean areNotificationsEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return notificationManager.areNotificationsEnabled();
        } else {
            // For older versions, permissions are always granted if notifications are not blocked in settings.
            return true; // Assume notifications are enabled.
        }
    }

    private void showEnableNotificationsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Enable Notifications")
                .setMessage("To enable system notifications, you need to turn them on in the app settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    // Open the app's notification settings
                    Intent intent = new Intent();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("android.provider.extra.APP_PACKAGE", requireContext().getPackageName());
                    } else {
                        // Fallback for older Android versions
                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                        intent.setData(uri);
                    }
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


    private void showDisableNotificationsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Disable Notifications")
                .setMessage("To stop system notifications, you need to turn them off in the app settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    // Open the app's notification settings
                    Intent intent = new Intent();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("android.provider.extra.APP_PACKAGE", requireContext().getPackageName());
                    } else {
                        // Fallback for older Android versions
                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                        intent.setData(uri);
                    }
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check and update notification button icon when returning to the fragment
        updateNotificationButtonIcon();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
