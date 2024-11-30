package com.example.cmput301project.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmput301project.FirebaseServer;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.ImageRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminManageImagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button deleteButton;
    private SearchView searchView;
    private ImageRecyclerViewAdapter adapter;
    private List<String> imageUrls;

    private ProgressBar progressBar;
    private ConstraintLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_posters);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerView);
        deleteButton = findViewById(R.id.deletePoster);
        searchView = findViewById(R.id.searchView);
        progressBar = findViewById(R.id.progressBar);
        mainLayout = findViewById(R.id.main_layout);

        CardView container = findViewById(R.id.searchPoster);
        container.setVisibility(View.GONE);
//        searchView.setVisibility(View.GONE);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        imageUrls = new ArrayList<>();
        adapter = new ImageRecyclerViewAdapter(this, new ArrayList<>(), new ImageRecyclerViewAdapter.OnImageDeleteListener() {
            @Override
            public void onDelete(String imageUrl) {
                onImageDelete(imageUrl); // Handle single image deletion
            }

            @Override
            public void onBatchDelete(List<String> imageUrls) {
                for (String imageUrl : imageUrls) {
                    onImageDelete(imageUrl); // Handle batch image deletion by calling the same method
                }
            }
        });
        recyclerView.setAdapter(adapter);

        // Fetch and display poster URLs
        FirebaseServer firebaseServer = new FirebaseServer();
//        firebaseServer.retrieveAllPosterUrls(new FirebaseServer.OnImagesRetrievedListener() {
//            @Override
//            public void onImagesRetrieved(List<String> images) {
//                imageUrls.clear();
//                imageUrls.addAll(images);
//                adapter.updateImageList(imageUrls);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Toast.makeText(AdminManageImagesActivity.this, "Failed to load posters.", Toast.LENGTH_SHORT).show();
//                Log.e("AdminManageImages", "Error loading posters", e);
//            }
//        });
        firebaseServer.retrieveAllImages(new FirebaseServer.OnImagesRetrievedListener() {
            @Override
            public void onImagesRetrieved(List<String> images) {
                imageUrls.clear();
                imageUrls.addAll(images);
                adapter.updateImageList(imageUrls);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminManageImagesActivity.this, "Failed to load images.", Toast.LENGTH_SHORT).show();
                Log.e("AdminManageImages", "Error loading images", e);
            }
        });

        // Set up SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterImages(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterImages(newText);
                return true;
            }
        });

        deleteButton.setOnClickListener(v -> {
            lockUI();
            Log.d("lockui", "debug3");
            List<String> selectedImages = adapter.getSelectedImages();
            if (selectedImages.isEmpty()) {
                Toast.makeText(this, "No images selected for deletion.", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseServer.deleteImage(selectedImages.get(0), new FirebaseServer.OnImageDeletedListener() {
                @Override
                public void onImageDeleted() {
                    unlockUI();
                    imageUrls.removeAll(selectedImages);
                    adapter.updateImageList(imageUrls);
                    adapter.clearSelections();
                    Toast.makeText(AdminManageImagesActivity.this, "Deleted selected images.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    unlockUI();
                    Toast.makeText(AdminManageImagesActivity.this, "Failed to delete images.", Toast.LENGTH_SHORT).show();
                    Log.e("AdminManageImages", "Error deleting images", e);
                }
            });
        });
    }

    private void filterImages(String query) {
        List<String> filteredList = new ArrayList<>();
        for (String url : imageUrls) {
            if (url.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(url);
            }
        }
        adapter.updateImageList(filteredList);
    }

    private void onImageDelete(String imageUrl) {
        FirebaseServer firebaseServer = new FirebaseServer();
        firebaseServer.deleteImage(imageUrl, new FirebaseServer.OnImageDeletedListener() {
            @Override
            public void onImageDeleted() {
                Toast.makeText(AdminManageImagesActivity.this, "Image deleted successfully.", Toast.LENGTH_SHORT).show();
                imageUrls.remove(imageUrl);
                adapter.updateImageList(imageUrls);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminManageImagesActivity.this, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AdminManageImages", "Error deleting image", e);
            }
        });
    }

    private void lockUI() {
        progressBar.setVisibility(View.VISIBLE);
        mainLayout.setAlpha(0.5f); // Dim background for effect
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void unlockUI() {
        progressBar.setVisibility(View.GONE);
        mainLayout.setAlpha(1.0f); // Restore background opacity
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
