package com.example.cmput301project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.databinding.OrganizerEventListBinding;
import com.example.cmput301project.databinding.OrganizerHomepageBinding;

public class OrganizerEventListFragment extends Fragment {
    private OrganizerEventListBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        binding = OrganizerEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.addEventButton.setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerEventListFragment.this)
                        .navigate(R.id.action_OrganizerEventList_to_AddEvent)
        );
    }
}
