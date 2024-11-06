package com.example.cmput301project.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.MyApplication;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.UserController;
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
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

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
                })
        );

        binding.scanQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ScannerActivity.class);
                startActivity(intent);
            }
        });

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
