//code review from ChatGPT
package com.example.cmput301project.pooling;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301project.R;
import com.example.cmput301project.service.PoolingService;
// this part is for the pooling activity, and the draw button. it will draw the entrants and listen for declines
public class PoolingActivity extends AppCompatActivity {

    private static final String TAG = "PoolingActivity";

    private EditText etEventId;
    private Button btnDraw;
    private PoolingService poolingService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pooling);

        etEventId = findViewById(R.id.etEventId);
        btnDraw = findViewById(R.id.btnDraw);
        poolingService = new PoolingService();

        if (btnDraw == null) {
            Log.e(TAG, "btnDraw is null. Check the layout file for correct id.");
            Toast.makeText(this, "Button not found in layout.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnDraw.setOnClickListener(v -> {
            String eventId = etEventId.getText().toString().trim();
            Log.d(TAG, "Draw button clicked with eventId: " + eventId);

            if (TextUtils.isEmpty(eventId)) {
                Toast.makeText(PoolingActivity.this, "Event ID is required", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Event ID is empty.");
                return;
            }

            Toast.makeText(PoolingActivity.this, "Processing...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Calling PoolingService methods for eventId: " + eventId);

            poolingService.drawEntrants(eventId);
            poolingService.listenForDeclines(eventId);

            Toast.makeText(PoolingActivity.this, "Lottery and Classes processing started", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "PoolingService methods called successfully.");
        });
    }
}
