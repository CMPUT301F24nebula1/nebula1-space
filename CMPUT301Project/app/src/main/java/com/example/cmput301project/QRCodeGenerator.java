package com.example.cmput301project;

import android.graphics.Bitmap;
import android.util.Base64;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generates QR code.
 * @author Xinjia Fan
 */
public class QRCodeGenerator {
    // Method to generate the QR code for the event
    public static Bitmap generateQRCode(String eventId) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            BitMatrix bitMatrix = barcodeEncoder.encode(eventId, BarcodeFormat.QR_CODE, 400, 400);
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);  // Compress to PNG
        return byteArrayOutputStream.toByteArray();
    }

    public static String hashQRCode(byte[] qrCodeBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(qrCodeBytes);
            return Base64.encodeToString(hash, Base64.DEFAULT);  // Encode as Base64 for storage
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}
