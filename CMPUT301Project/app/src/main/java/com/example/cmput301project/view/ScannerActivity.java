package com.example.cmput301project.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.cmput301project.R;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.io.IOException;

/**
 * Activity for scanning the QR code
 * @author Xinjia Fan
 */
public class ScannerActivity extends Activity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private DecoratedBarcodeView barcodeScannerView;

    Event e;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        db = FirebaseFirestore.getInstance();

        barcodeScannerView = findViewById(R.id.barcode_scanner);
        Button closeButton = findViewById(R.id.close_button);
        Button selectFromGalleryButton = findViewById(R.id.select_from_gallery_button);

        closeButton.setOnClickListener(v -> finish()); // 关闭按钮

        selectFromGalleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        barcodeScannerView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                String scannedResult = result.getText();
                Log.d("CustomScannerActivity", "Scanned Result: " + scannedResult);

//                Intent intent = new Intent(ScannerActivity.this, MainActivity.class);
//                intent.putExtra("eventId", scannedResult);
//                //intent.putExtra("event", findEventInAllOrganizers(scannedResult));
//                intent.putExtra("navigateTo", "eventDetailFragment");
//                startActivity(intent);

                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                String qrCodeContent = decodeQRCodeFromBitmap(bitmap);
                if (qrCodeContent != null) {
                    Log.d("CustomScannerActivity", "QR Code from Gallery: " + qrCodeContent);

                    Intent intent = new Intent(ScannerActivity.this, MainActivity.class);
                    intent.putExtra("eventId", qrCodeContent);
                    intent.putExtra("navigateTo", "entrantEventViewFragment");
                    startActivity(intent);

                } else {
                    Log.d("CustomScannerActivity", "No QR Code found in the selected image");
                }
            } catch (IOException e) {
                Log.e("CustomScannerActivity", "Error decoding image", e);
            }
        }
    }

    private String decodeQRCodeFromBitmap(Bitmap bitmap) {
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            Result result = reader.decode(binaryBitmap);
            Log.e("CustomScannerActivity", "Success-----------------------------------------------");
            return result.getText();
        } catch (ChecksumException | FormatException | NotFoundException e) {
            Log.e("CustomScannerActivity", "QR Code decoding failed", e);
        }
        return null;
    }

    public Event findEventInAllOrganizers(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference organizersRef = db.collection("organizers");

        organizersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Organizer organizer = document.toObject(Organizer.class);
                    if (organizer != null && organizer.getEvents() != null) {
                        for (Event event : organizer.getEvents()) {
                            if (event.getId().equals(eventId)) {
                                Log.d("Firestore", "Found event with ID: " + eventId + " in organizer: " + organizer.getId());
                                e = event;
                                return;
                            }
                        }
                    }
                }
                Log.d("Firestore", "No matching event found with ID: " + eventId);
            } else {
                Log.w("Firestore", "Error getting documents", task.getException());
            }
        });
        return e;
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }
}

