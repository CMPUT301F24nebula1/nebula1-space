package com.example.cmput301project;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.databinding.EntrantHomepageBinding;

public class EntrantHomepageFragment extends Fragment {
    private EntrantHomepageBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        binding = EntrantHomepageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.profileButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EntrantHomepageFragment.this)
                        .navigate(R.id.action_EntrantHomepage_to_EntrantProfile)
        );

        binding.organizerViewButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EntrantHomepageFragment.this)
                        .navigate(R.id.action_EntrantHomepage_to_OrganizerHomepage)
        );

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
