package com.example.cmput301project.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.MyApplication;
import com.example.cmput301project.R;
import com.example.cmput301project.databinding.OrganizerHomepageBinding;
import com.example.cmput301project.model.Organizer;

/**
 * Fragment for organizer homepage.
 * @author Xinjia Fan
 */
public class OrganizerHomepageFragment extends Fragment {
    private OrganizerHomepageBinding binding;

    private Organizer organizer;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = OrganizerHomepageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.manageEventsButton.setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerHomepageFragment.this)
                        .navigate(R.id.action_OrganizerHomepage_to_EventList)
        );

        binding.manageFacilityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(OrganizerHomepageFragment.this)
                        .navigate(R.id.OrganizerFacilityProfileFragment);
            }
        });

        MyApplication app = (MyApplication) requireActivity().getApplication();

        organizer = app.getOrganizerLiveData().getValue();

        if (organizer != null) {
            try {
                if (organizer.getName().isEmpty() && organizer.getName() != null) {
                    binding.manageFacilityBtn.setText("Create Facility");
                    binding.manageEventsButton.setEnabled(false);
                    binding.manageEventsButton.setAlpha(0.3f);
                }
                else {
                    binding.manageFacilityBtn.setText("Manage Facility");
                    binding.manageEventsButton.setEnabled(true);
                }
            } catch (Exception e) {
                Log.d("organizer", "name is null");
                binding.manageFacilityBtn.setText("Create Facility");
                binding.manageEventsButton.setEnabled(false);
                binding.manageEventsButton.setAlpha(0.3f);
            }
        }

        app.getOrganizerLiveData().observe(getViewLifecycleOwner(), organizer1 -> {
            organizer = organizer1;
            if (organizer != null) {
                try {
                    if (organizer.getName().isEmpty() && organizer.getName() != null) {
                        binding.manageFacilityBtn.setText("Create Facility");
                        binding.manageEventsButton.setEnabled(false);
                        binding.manageEventsButton.setAlpha(0.3f);
                    }
                    else {
                        binding.manageFacilityBtn.setText("Manage Facility");
                        binding.manageEventsButton.setEnabled(true);
                        binding.manageEventsButton.setAlpha(1f);
                    }
                } catch (Exception e) {
                    Log.d("organizer", "name is null");
                    binding.manageFacilityBtn.setText("Create Facility");
                    binding.manageEventsButton.setEnabled(false);
                    binding.manageEventsButton.setAlpha(0.3f);
                }
            }
        });
    }
}
