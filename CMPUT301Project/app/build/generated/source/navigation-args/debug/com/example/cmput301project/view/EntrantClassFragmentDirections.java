package com.example.cmput301project.view;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Event;
import java.io.Serializable;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;

public class EntrantClassFragmentDirections {
  private EntrantClassFragmentDirections() {
  }

  @NonNull
  public static ActionEntrantClassToEntrantEventView actionEntrantClassToEntrantEventView(
      @NonNull Event e, @NonNull String category) {
    return new ActionEntrantClassToEntrantEventView(e, category);
  }

  public static class ActionEntrantClassToEntrantEventView implements NavDirections {
    private final HashMap arguments = new HashMap();

    @SuppressWarnings("unchecked")
    private ActionEntrantClassToEntrantEventView(@NonNull Event e, @NonNull String category) {
      if (e == null) {
        throw new IllegalArgumentException("Argument \"e\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("e", e);
      if (category == null) {
        throw new IllegalArgumentException("Argument \"category\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("category", category);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionEntrantClassToEntrantEventView setE(@NonNull Event e) {
      if (e == null) {
        throw new IllegalArgumentException("Argument \"e\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("e", e);
      return this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ActionEntrantClassToEntrantEventView setCategory(@NonNull String category) {
      if (category == null) {
        throw new IllegalArgumentException("Argument \"category\" is marked as non-null but was passed a null value.");
      }
      this.arguments.put("category", category);
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public Bundle getArguments() {
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
      if (arguments.containsKey("category")) {
        String category = (String) arguments.get("category");
        __result.putString("category", category);
      }
      return __result;
    }

    @Override
    public int getActionId() {
      return R.id.action_EntrantClass_to_EntrantEventView;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public Event getE() {
      return (Event) arguments.get("e");
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public String getCategory() {
      return (String) arguments.get("category");
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) {
          return true;
      }
      if (object == null || getClass() != object.getClass()) {
          return false;
      }
      ActionEntrantClassToEntrantEventView that = (ActionEntrantClassToEntrantEventView) object;
      if (arguments.containsKey("e") != that.arguments.containsKey("e")) {
        return false;
      }
      if (getE() != null ? !getE().equals(that.getE()) : that.getE() != null) {
        return false;
      }
      if (arguments.containsKey("category") != that.arguments.containsKey("category")) {
        return false;
      }
      if (getCategory() != null ? !getCategory().equals(that.getCategory()) : that.getCategory() != null) {
        return false;
      }
      if (getActionId() != that.getActionId()) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + (getE() != null ? getE().hashCode() : 0);
      result = 31 * result + (getCategory() != null ? getCategory().hashCode() : 0);
      result = 31 * result + getActionId();
      return result;
    }

    @Override
    public String toString() {
      return "ActionEntrantClassToEntrantEventView(actionId=" + getActionId() + "){"
          + "e=" + getE()
          + ", category=" + getCategory()
          + "}";
    }
  }
}
