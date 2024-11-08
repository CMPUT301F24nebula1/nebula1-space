package com.example.cmput301project.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Entrant;

import java.util.ArrayList;

public class EntrantArrayAdapter extends ArrayAdapter<Entrant> {

    private Context context;

    public EntrantArrayAdapter(@NonNull Context context, @NonNull ArrayList<Entrant> entrants) {
        super(context, 0, entrants);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.participant_list_item,
                    parent, false);
        } else {
            view = convertView;
        }
        Entrant e = getItem(position);
        ImageView profile = view.findViewById(R.id.entrant_profile);
        TextView name = view.findViewById(R.id.entrant_name);
        CheckBox select = view.findViewById(R.id.select_participant_checkbox);
        if (e != null) {
            name.setText(e.getName());

            if (!(e.getProfilePictureUrl() == null) && !e.getProfilePictureUrl().isEmpty()) {
                Glide.with(getContext())
                        .load(e.getProfilePictureUrl())
                        .placeholder(R.drawable.placeholder_image)  // placeholder
                        .error(R.drawable.error_image)              // error image
                        .into(profile);
            }
            else {
                Glide.with(getContext())
                        .load(createInitialsDrawable(e.getName()))
                        .placeholder(R.drawable.placeholder_image)  // placeholder
                        .error(R.drawable.error_image)              // error image
                        .into(profile);
                Log.e("Error", "Profile picture URL is null");
            }
        }
        return view;
    }

    private String getInitials(String name) {
        if (TextUtils.isEmpty(name)) return "";
        String[] parts = name.trim().split(" ");
        String initials = "";
        for (String part : parts) {
            if (!TextUtils.isEmpty(part)) {
                initials += part.charAt(0);
            }
        }
        return initials.toUpperCase();
    }

    private BitmapDrawable createInitialsDrawable(String name) {
        String initials = getInitials(name);
        int size = 150;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(50f);
        canvas.drawText(initials, size / 2, size / 2 + 15, paint);

//        int MyVersion = Build.VERSION.SDK_INT;
//        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
//            checkIfAlreadyhavePermission();
//        }
//        imageUri = getImageUri(this.getContext(), bitmap);

        return new BitmapDrawable(context.getResources(), bitmap);
    }


}
