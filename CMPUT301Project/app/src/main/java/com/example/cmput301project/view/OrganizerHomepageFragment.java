package com.example.cmput301project.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.R;
import com.example.cmput301project.databinding.OrganizerHomepageBinding;

/**
 * Fragment for organizer homepage.
 * @author Xinjia Fan
 */
public class OrganizerHomepageFragment extends Fragment {
    private OrganizerHomepageBinding binding;

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

    }
}
