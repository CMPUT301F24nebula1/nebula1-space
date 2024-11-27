// Generated by view binder compiler. Do not edit!
package com.example.cmput301project.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.cmput301project.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class OrganizerHomepageBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final Button manageEventsButton;

  @NonNull
  public final Button manageFacilityBtn;

  private OrganizerHomepageBinding(@NonNull ConstraintLayout rootView,
      @NonNull Button manageEventsButton, @NonNull Button manageFacilityBtn) {
    this.rootView = rootView;
    this.manageEventsButton = manageEventsButton;
    this.manageFacilityBtn = manageFacilityBtn;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static OrganizerHomepageBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static OrganizerHomepageBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.organizer_homepage, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static OrganizerHomepageBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.manageEventsButton;
      Button manageEventsButton = ViewBindings.findChildViewById(rootView, id);
      if (manageEventsButton == null) {
        break missingId;
      }

      id = R.id.manage_facility_btn;
      Button manageFacilityBtn = ViewBindings.findChildViewById(rootView, id);
      if (manageFacilityBtn == null) {
        break missingId;
      }

      return new OrganizerHomepageBinding((ConstraintLayout) rootView, manageEventsButton,
          manageFacilityBtn);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
