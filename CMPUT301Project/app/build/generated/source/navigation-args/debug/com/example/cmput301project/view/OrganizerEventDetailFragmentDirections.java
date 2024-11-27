package com.example.cmput301project.view;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.example.cmput301project.R;

public class OrganizerEventDetailFragmentDirections {
  private OrganizerEventDetailFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionOrganizerEventDetailFragmentToOrganizerHomepage() {
    return new ActionOnlyNavDirections(R.id.action_OrganizerEventDetailFragment_to_OrganizerHomepage);
  }
}
