package com.example.cmput301project;

import android.app.Application;
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
    private MyApplication app;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        binding = EntrantHomepageBinding.inflate(inflater, container, false);
        app = (MyApplication) requireActivity().getApplication();
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.profileButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EntrantHomepageFragment.this)
                        .navigate(R.id.action_EntrantHomepage_to_EntrantProfile)
        );

        binding.organizerViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserController.updateUserRole(app.getUserId(), "organizer");
                NavHostFragment.findNavController(EntrantHomepageFragment.this)
                        .navigate(R.id.action_EntrantHomepage_to_OrganizerHomepage);
            }
        });

        binding.scanQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
