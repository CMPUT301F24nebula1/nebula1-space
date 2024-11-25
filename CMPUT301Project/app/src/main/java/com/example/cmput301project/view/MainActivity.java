package com.example.cmput301project.view;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;

import android.location.Location;
import android.location.LocationListener;
import androidx.annotation.NonNull;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

//import com.example.cmput301project.Manifest;
import com.example.cmput301project.MyApplication;
import com.example.cmput301project.controller.LocationHelper;
import com.example.cmput301project.controller.UserController;
import com.example.cmput301project.model.Notification;
import com.example.cmput301project.model.Organizer;
import com.example.cmput301project.R;
import com.example.cmput301project.model.User;
import com.example.cmput301project.databinding.ActivityMainBinding;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;
import java.util.Map;


/**
 * MainActivity
 * @author Xinjia Fan
 * @author Zaid Islam
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private FirebaseFirestore db;
    private CollectionReference userRef;
    private String id;

    // manages whether it's entrant homepage or organizer homepage
    private MaterialButtonToggleGroup toggleGroup;

    //constant for location permission requests (301)
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 301;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        id = getDeviceId(this);

        id = "1d98b5f2ca50879e";

//        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
//                .setPersistenceEnabled(true)
//                .build();
//        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        ((MyApplication) this.getApplication()).setUserId(id);
        ((MyApplication) this.getApplication()).setDb(FirebaseFirestore.getInstance());
        db = ((MyApplication) this.getApplication()).getDb();
        retrieveUser(id);
        ((MyApplication) this.getApplication()).listenToOrganizerFirebaseUpdates(id);
        ((MyApplication) this.getApplication()).listenToEntrantFirebaseUpdates(id);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
//        setSupportActionBar(findViewById(R.id.toolbar));

        Intent intent = getIntent();
        String navigateTo = intent.getStringExtra("navigateTo");
        String eventId = intent.getStringExtra("eventId");

        Log.d("MainActivity", "navigateTo: " + navigateTo + ", eventId: " + eventId);

        Button adminModeButton = findViewById(R.id.btn_admin);
        adminModeButton.setOnClickListener(view -> {
            checkIfUserIsAdmin(isAdmin -> {
                if (isAdmin) {
                    // If the user is an admin, navigate to the admin activity
                    Intent adminIntent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                    startActivity(adminIntent);
                } else {
                    // Show access denied message
                    Toast.makeText(MainActivity.this, "Access Denied: Admins Only", Toast.LENGTH_SHORT).show();
                }
            });
        });

        checkIfUserIsAdmin(isAdmin -> {
            if (!isAdmin) {
                // Show access denied message
                binding.btnAdmin.setVisibility(View.GONE);
            }
        });

        toggleGroup = findViewById(R.id.toggleGroup);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Set the initial fragment (e.g., EntrantFragment)
        if (savedInstanceState == null) {
            replaceFragment(new EntrantHomepageFragment());
            toggleGroup.check(R.id.btn_entrant); // Set Entrant as default selected
        }

        // Add listener to toggle group for switching fragments
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_organizer) {
                    Log.d("Navigation", "Navigating to Organizer Homepage");
                    // Navigate to OrganizerFragment
                    UserController.updateUserRole(id, "organizer");

                    if (navController.getCurrentDestination() != null &&
                            navController.getCurrentDestination().getId() == R.id.EntrantHomepageFragment) {
                        navController.navigate(R.id.action_EntrantHomepage_to_OrganizerHomepage);
                    } else {
                        Log.e("NavigationError", "Cannot navigate to Organizer Homepage. Current destination mismatch.");
                    }
                } else if (checkedId == R.id.btn_entrant) {
                    Log.d("Navigation", "Navigating to Entrant Homepage");
                    // Navigate to EntrantFragment
                    if (navController.getCurrentDestination() != null &&
                            navController.getCurrentDestination().getId() == R.id.OrganizerHomepageFragment) {
                        navController.navigate(R.id.action_OrganizerHomepage_to_EntrantHomepage);
                    } else {
                        Log.e("NavigationError", "Cannot navigate to Entrant Homepage. Current destination mismatch.");
                    }
                }
            }
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // hide toolbar if it's homepage
            if (destination.getId() == R.id.EntrantHomepageFragment || destination.getId() == R.id.OrganizerHomepageFragment) {
                toolbar.setVisibility(View.GONE);
            } else {
                toolbar.setVisibility(View.VISIBLE);
            }

            if (destination.getId() == R.id.OrganizerHomepageFragment || destination.getId() == R.id.EntrantHomepageFragment) {
                toggleGroup.setVisibility(View.VISIBLE); // Show toggle group on Organizer and Entrant fragments
            } else {
                toggleGroup.setVisibility(View.GONE); // Hide toggle group on other fragments
            }
        });

        if ("entrantEventViewFragment".equals(navigateTo)) {
            Log.d("MainActivity", "Navigating to entrantEventView");
            findEventInAllOrganizers(eventId, navController);
        }


        createNotificationChannel(this);


        //checking location permissions
        if (checkLocationPermission()) {
            startLocationUpdates();
        }
    }
    public void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default_channel_id";
            CharSequence channelName = "Default Channel";
            String channelDescription = "Channel for app notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow();;
    }

    public void findEventInAllOrganizers(String eventId, NavController nc) {
        Log.d("wishlist before nav1", ((MyApplication) this.getApplication()).getEntrantLiveData().getValue().getWaitlistEventIds().toString());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference organizersRef = db.collection("organizers");
        Log.d("wishlist before nav2", ((MyApplication) this.getApplication()).getEntrantLiveData().getValue().getWaitlistEventIds().toString());
        organizersRef.get().addOnCompleteListener(task -> {
            Log.d("wishlist before nav3", ((MyApplication) this.getApplication()).getEntrantLiveData().getValue().getWaitlistEventIds().toString());
            if (task.isSuccessful() && task.getResult() != null) {

                for (QueryDocumentSnapshot organizerDoc : task.getResult()) {
                    String organizerId = organizerDoc.getId();

                    // Get the "events" subcollection for the current organizer
                    CollectionReference eventsRef = organizersRef.document(organizerId).collection("events");

                    eventsRef.document(eventId).get().addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful() && eventTask.getResult() != null && eventTask.getResult().exists()) {
                            Event event = eventTask.getResult().toObject(Event.class);

                            ArrayList<String> userIdList = new ArrayList<>();

                            eventsRef.document(event.getId()).collection("userId").get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                                            // field "userId" that stores a string
                                            String userId = document.getString("userId");
                                            if (userId != null) {
                                                userIdList.add(userId);
                                            }
                                        }
                                        event.setWaitlistEntrantIds(userIdList);
                                        Log.d("wishlist of event1", event.getWaitlistEntrantIds().toString());
                                        if (event != null) {
                                            Log.d("Firestore", "Found event with ID: " + eventId + " in organizer: " + organizerId);
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("e", event);
                                            nc.navigate(R.id.action_EntrantHomepage_to_EntrantEventView, bundle);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle any errors here
                                        Log.e("FirestoreError", "Error retrieving user IDs", e);
                                    });
                            Log.d("wishlist of event2", event.getWaitlistEntrantIds().toString());


                        } else if (eventTask.isSuccessful() && (eventTask.getResult() == null || !eventTask.getResult().exists())) {
                            Log.d("Firestore", "No matching event found with ID: " + eventId + " in organizer: " + organizerId);
                        } else {
                            Log.w("Firestore", "Error getting event", eventTask.getException());
                        }
                    });
                }
            } else {
                Log.w("Firestore", "Error getting organizers", task.getException());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notification_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set the Entrant button as selected in the toggle group
        if (toggleGroup.getVisibility() == View.VISIBLE && toggleGroup.getCheckedButtonId() != R.id.btn_entrant) {
            toggleGroup.check(R.id.btn_entrant); // Set Entrant as the default selection
        }
    }


    private String getDeviceId(Context context) {
        // Retrieve ANDROID_ID as the device ID
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void retrieveUser(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                ArrayList<String> roles = (ArrayList<String>) documentSnapshot.get("role");
                if (roles != null && roles.contains("entrant")) {
                    // set up entrant part for this user

                    db.collection("entrants").document(userId).get()
                            .addOnSuccessListener(entrantSnapshot -> {
                                if (entrantSnapshot.exists()) {
                                    Entrant entrant = entrantSnapshot.toObject(Entrant.class);
                                    retrieveEntrantWishlist(entrant);
//                                    ((MyApplication) getApplication()).setEntrantLiveData(entrant);
                                }
                                else {
                                    Entrant entrant = new Entrant(userId);
                                    ((MyApplication) getApplication()).setEntrantLiveData(entrant);
                                }
                            })
                            .addOnFailureListener(e -> Log.w("Firestore", "Error retrieving entrant data", e));

                }
                if (roles != null && roles.contains("organizer")) {
                    // set up organizer part for this user

                    db.collection("organizers").document(userId).get()
                            .addOnSuccessListener(organizerSnapshot -> {
                                if (organizerSnapshot.exists()) {
                                    Organizer organizer = organizerSnapshot.toObject(Organizer.class);

                                    if (organizer != null) {
                                        // Retrieve events from the subcollection
                                        CollectionReference eventsRef = db.collection("organizers").document(userId).collection("events");
                                        eventsRef.get()
                                                .addOnSuccessListener(eventsSnapshot -> {
                                                    ArrayList<Event> eventsList = new ArrayList<>();
                                                    for (QueryDocumentSnapshot eventDoc : eventsSnapshot) {
                                                        Event event = eventDoc.toObject(Event.class);

                                                        eventsRef.document(event.getId()).get().addOnCompleteListener(eventTask -> {
                                                            if (eventTask.isSuccessful() && eventTask.getResult() != null && eventTask.getResult().exists()) {
//                                                                Event event = eventTask.getResult().toObject(Event.class);

                                                                ArrayList<String> wishlishIds = new ArrayList<>();

                                                                eventsRef.document(event.getId()).collection("userId").get()
                                                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                                                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                                                                // field "userId" that stores a string
                                                                                String entrantId = document.getString("userId");
                                                                                if (entrantId != null) {
                                                                                    wishlishIds.add(entrantId);
                                                                                }
                                                                            }
                                                                            event.setWaitlistEntrantIds(wishlishIds);
                                                                            if (event != null) {
                                                                                Log.d("Firestore", "Found event with ID: " + event.getId() + " in organizer: " + organizer.getId());
                                                                                eventsList.add(event);
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(e -> {
                                                                            // Handle any errors here
                                                                            Log.e("FirestoreError", "Error retrieving user IDs", e);
                                                                        });

                                                            } else if (eventTask.isSuccessful() && (eventTask.getResult() == null || !eventTask.getResult().exists())) {
                                                                Log.d("Firestore", "No matching event found with ID: " + event.getId() + " in organizer: " + organizer.getId());
                                                            } else {
                                                                Log.w("Firestore", "Error getting event", eventTask.getException());
                                                            }
                                                        });
                                                    }

                                                    // Set the events list in the organizer object
                                                    organizer.setEvents(eventsList);

                                                    // Update the global instance and LiveData
                                                    ((MyApplication) getApplication()).setOrganizer(organizer);
                                                    ((MyApplication) getApplication()).setOrganizerLiveData(organizer);

                                                    Log.d("Firestore", "Organizer and events successfully loaded.");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w("Firestore", "Error retrieving events data", e);
                                                });
                                    }
                                } else {
                                    Log.w("Firestore", "Organizer document does not exist.");
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Firestore", "Error retrieving organizer data", e);
                            });

                }

            } else {
                User u = new User(userId);
                addUser(u);
                addEntrant(new Entrant(userId));
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Error retrieving user", e);
        });
    }

    private void retrieveEntrantWishlist(Entrant entrant) {
        CollectionReference waitlistRef = db.collection("entrants").document(entrant.getId()).collection("entrantWaitList");
        ArrayList<String> wishlist = new ArrayList<>();

        waitlistRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String item = document.getString("eventId");
                                if (item != null) {
                                    wishlist.add(item);
                                }
                            }
                            // Now `wishlist` contains all items
                            Log.d("Wishlist main", "Wishlist items: " + wishlist);
                            entrant.setWaitlistEventIds(wishlist);
                            retrieveEntrantNotification(entrant, new MyApplication.NotificationCallback() {
                                @Override
                                public void onNotificationsRetrieved(ArrayList<Notification> notifications) {
                                    ((MyApplication) getApplication()).setEntrantLiveData(entrant);
                                    Log.d("retrieve entrant", "succeed");


                                    int i = 0;
                                    if (!entrant.getReceiveNotification()) {
                                        return;
                                    }
                                    for (Notification notification: notifications) {
                                        if (!notification.isRead()) {
                                            Log.d("notification", "count");
                                            showNotification("New Notifications", notification.getMessage(), i);
                                            i++;
                                        }
                                    }

                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("retrieve entrant", e.toString());
                                }
                            });
                        } else {
                            Log.e("Firestore Error", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void showNotification(String title, String message, int id) {
        String channelId = "default_channel_id";
        // Create a notification channel (Required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Default Channel",
                    NotificationManager.IMPORTANCE_HIGH // Set importance to HIGH for heads-up notifications
            );
            channel.setDescription("This is the default notification channel");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification) // Replace with your app's small icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Set priority to HIGH
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Enable sound and vibration
                .setAutoCancel(true); // Dismiss notification on click
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Show the notification
        notificationManager.notify(id, builder.build());
    }

    private void retrieveEntrantNotification(Entrant entrant, MyApplication.NotificationCallback callback) {
        CollectionReference notificationRef = db.collection("entrants")
                .document(entrant.getId())
                .collection("notifications");
        ArrayList<Notification> notifications = new ArrayList<>();

        notificationRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e("Firestore Error", "Error listening to notification updates", error);
                return;
            }

            if (snapshots != null) {
                notifications.clear();
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    Notification item = document.toObject(Notification.class);
                    if (item != null) {
                        notifications.add(item);
                    }
                }
                Log.d("Notifications", "Notifications: " + notifications);
                notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
                entrant.setNotifications(notifications);
//                setEntrantLiveData(entrant);

                if (callback != null) {
                    callback.onNotificationsRetrieved(notifications);
                }
            }
        });
    }

    public void addUser(User user) {
        db.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User successfully added!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding user", e);
                });
    }

    public void addEntrant(Entrant e) {
        db.collection("entrants").document(e.getId())
                .set(e)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User successfully added!");
                })
                .addOnFailureListener(f -> {
                    Log.w("Firestore", "Error adding user", f);
                });
    }

    private void checkIfUserIsAdmin(OnAdminCheckListener listener) {
        ((MyApplication) getApplication()).getUserRoles(new MyApplication.OnRolesLoadedListener() {
            @Override
            public void onRolesLoaded(ArrayList<String> roles) {
                boolean isAdmin = roles != null && roles.contains("admin");
                listener.onAdminCheckCompleted(isAdmin);
            }
        });
    }

    public interface OnAdminCheckListener {
        void onAdminCheckCompleted(boolean isAdmin);
    }

    // for ui test
    public void setId(String id) {
        this.id = id;
    }

    //    geolocation stuff
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return false; //no permissions
        }
        return true; //permission granted
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted, start location updates
                startLocationUpdates();
            } else {
                // permission denied, notify user
                Toast.makeText(this, "Location permission is required for this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        LocationHelper locationHelper = new LocationHelper(this);
        locationHelper.requestLocationUpdates(new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d("Location", "Lat: " + latitude + ", Long: " + longitude);

                // save lat and long to firestore
                saveLocationToFirestore(latitude, longitude);
            }
        });
    }

    private void saveLocationToFirestore(double latitude, double longitude) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = id; // Replace with the current user ID or entrant ID
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);

        db.collection("locations").document(userId)
                .set(locationData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Location saved successfully"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving location", e));
    }
}