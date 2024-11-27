package com.example.cmput301project.view;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavArgs;
import com.example.cmput301project.model.Event;
import java.io.Serializable;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;

public class OrganizerEventDetailFragmentArgs implements NavArgs {
  private final HashMap arguments = new HashMap();

  private OrganizerEventDetailFragmentArgs() {
  }

  @SuppressWarnings("unchecked")
  private OrganizerEventDetailFragmentArgs(HashMap argumentsMap) {
    this.arguments.putAll(argumentsMap);
  }

  @NonNull
  @SuppressWarnings({
      "unchecked",
      "deprecation"
  })
  public static OrganizerEventDetailFragmentArgs fromBundle(@NonNull Bundle bundle) {
    OrganizerEventDetailFragmentArgs __result = new OrganizerEventDetailFragmentArgs();
    bundle.setClassLoader(OrganizerEventDetailFragmentArgs.class.getClassLoader());
    if (bundle.containsKey("e")) {
      Event e;
      if (Parcelable.class.isAssignableFrom(Event.class) || Serializable.class.isAssignableFrom(Event.class)) {
        e = (Event) bundle.get("e");
      } else {
        throw new UnsupportedOperationException(Event.class.getName() + " must implement Parcelable or Serializable or must be an Enum.");
      }
      if (e == null) {
        throw new IllegalArgumentException("Argument \"e\" is marked as non-null but was passed a null value.");
      }
      __result.arguments.put("e", e);
    } else {
      throw new IllegalArgumentException("Required argument \"e\" is missing and does not have an android:defaultValue");
    }
    return __result;
  }

  @NonNull
  @SuppressWarnings("unchecked")
  public static OrganizerEventDetailFragmentArgs fromSavedStateHandle(
      @NonNull SavedStateHandle savedStateHandle) {
    OrganizerEventDetailFragmentArgs __result = new OrganizerEventDetailFragmentArgs();
    if (savedStateHandle.contains("e")) {
      Event e;
      e = savedStateHandle.get("e");
      if (e == null) {
        throw new IllegalArgumentException("Argument \"e\" is marked as non-null but was passed a null value.");
      }
      __result.arguments.put("e", e);
    } else {
      throw new IllegalArgumentException("Required argument \"e\" is missing and does not have an android:defaultValue");
    }
    return __result;
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public Event getE() {
    return (Event) arguments.get("e");
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public Bundle toBundle() {
    Bundle __result = new Bundle();
    if (arguments.containsKey("e")) {
      Event e = (Event) arguments.get("e");
      if (Parcelable.class.isAssignableFrom(Event.class) || e == null) {
        __result.putParcelable("e", Parcelable.class.cast(e));
      } else if (Serializable.class.isAssignableFrom(Event.class)) {
        __result.putSerializable("e", Serializable.class.cast(e));
      } else {
        throw new UnsupportedOperationException(Event.class.getName() + " must implement Parcelable or Serializable or must be an Enum.");
      }
    }
    return __result;
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public SavedStateHandle toSavedStateHandle() {
    SavedStateHandle __result = new SavedStateHandle();
    if (arguments.containsKey("e")) {
      Event e = (Event) arguments.get("e");
      if (Parcelable.class.isAssignableFrom(Event.class) || e == null) {
        __result.set("e", Parcelable.class.cast(e));
      } else if (Serializable.class.isAssignableFrom(Event.class)) {
        __result.set("e", Serializable.class.cast(e));
      } else {
        throw new UnsupportedOperationException(Event.class.getName() + " must implement Parcelable or Serializable or must be an Enum.");
      }
    }
    return __result;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
        return true;
    }
    if (object == null || getClass() != object.getClass()) {
        return false;
    }
    OrganizerEventDetailFragmentArgs that = (OrganizerEventDetailFragmentArgs) object;
    if (arguments.containsKey("e") != that.arguments.containsKey("e")) {
      return false;
    }
    if (getE() != null ? !getE().equals(that.getE()) : that.getE() != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + (getE() != null ? getE().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "OrganizerEventDetailFragmentArgs{"
        + "e=" + getE()
        + "}";
  }

  public static final class Builder {
    private final HashMap arguments = new HashMap();

    @SuppressWarnings("unchecked")
    public Builder(@NonNull OrganizerEventDetailFragmentArgs original) {
      this.arguments.putAll(original.arguments);
    }

    @SuppressWarnings("unchecked")
    public Builder(@NonNull Event e) {
      if (e == null) {
        throw new IllegalArgumentException("Argument \"e\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("e", e);
    }

    @NonNull
    public OrganizerEventDetailFragmentArgs build() {
      OrganizerEventDetailFragmentArgs result = new OrganizerEventDetailFragmentArgs(arguments);
      return result;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public Builder setE(@NonNull Event e) {
      if (e == null) {
        throw new IllegalArgumentException("Argument \"e\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("e", e);
      return this;
    }

    @SuppressWarnings({"unchecked","GetterOnBuilder"})
    @NonNull
    public Event getE() {
      return (Event) arguments.get("e");
    }
  }
}
