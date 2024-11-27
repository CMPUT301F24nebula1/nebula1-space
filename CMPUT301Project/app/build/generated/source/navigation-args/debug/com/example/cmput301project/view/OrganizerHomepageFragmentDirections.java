package com.example.cmput301project.view;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.example.cmput301project.R;

public class OrganizerHomepageFragmentDirections {
  private OrganizerHomepageFragmentDirections() {
  }

  @NonNull
  public static NavDirections actionOrganizerHomepageToEntrantHomepage() {
    return new ActionOnlyNavDirections(R.id.action_OrganizerHomepage_to_EntrantHomepage);
  }

  @NonNull
  public static NavDirections actionOrganizerHomepageToEventList() {
    return new ActionOnlyNavDirections(R.id.action_OrganizerHomepage_to_EventList);
  }

  @NonNull
  public static NavDirections actionOrganizerHomepageToFacilityProfile() {
    return new ActionOnlyNavDirections(R.id.action_OrganizerHomepage_to_FacilityProfile);
  }
}
