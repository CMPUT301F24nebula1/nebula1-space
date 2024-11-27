package com.example.cmput301project.view;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.example.cmput301project.R;

public class EntrantEventViewFragmentDirections {
  private EntrantEventViewFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionEntrantEventViewToEntrantHomepage() {
    return new ActionOnlyNavDirections(R.id.action_EntrantEventView_to_EntrantHomepage);
  }
}
