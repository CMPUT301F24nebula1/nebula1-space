package com.example.cmput301project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.databinding.EntrantHomepageBinding;
import com.example.cmput301project.databinding.OrganizerHomepageBinding;

public class OrganizerHomepageFragment extends Fragment {
    private OrganizerHomepageBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        binding = OrganizerHomepageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.entrantViewButton.setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerHomepageFragment.this)
                        .navigate(R.id.action_OrganizerHomepage_to_EntrantHomepage)
        );
        binding.manageEventsButton.setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerHomepageFragment.this)
                        .navigate(R.id.action_OrganizerHomepage_to_EventList)
        );

    }
}
