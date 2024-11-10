package com.example.cmput301project.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cmput301project.databinding.EmptyFragmentBinding;
import com.example.cmput301project.databinding.OrganizerEventViewBinding;

public class EmptyFragment extends Fragment {
    private EmptyFragmentBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = EmptyFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
