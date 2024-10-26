package com.example.cmput301project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cmput301project.databinding.EntrantProfileBinding;

public class EntrantProfileFragment extends Fragment {

    private EntrantProfileBinding binding;
    MyApplication app;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.entrant_profile, container, false);
        app = (MyApplication) requireActivity().getApplication();
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Entrant entrant = app.getEntrant();

        app.setEntrantLiveData(entrant);
    }
}
