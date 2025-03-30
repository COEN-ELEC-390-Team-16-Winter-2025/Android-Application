package com.drinkwise.app.ui.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.drinkwise.app.R;
import com.drinkwise.app.Recommendation;
import com.drinkwise.app.ScanningActivity;
import com.drinkwise.app.ui.home.drinklog.BACCalculator;
import com.drinkwise.app.ui.home.drinklog.DrinkLogItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class DashboardFragment extends Fragment {
    private static final String TAG = "DashboardFragment";

    // Top Section Views
    private TextView bacLevel;           // Shows BAC level as a percentage (e.g., 0.06%)
    private ProgressBar bacProgressBar;  // Horizontal progress bar for BAC level
    private TextView bacStatus;          // Text status like "Safe", "Caution", "Danger"

    // Alcohol counters
    private TextView beerCount;
    private TextView wineCount;
    private TextView champagneCount;
    private TextView cocktailCount;
    private TextView shotCount;
    private TextView sakeCount;
    private Button addBeerButton;
    private Button addWineButton;
    private Button addChampagneButton;
    private Button addCocktailButton;
    private Button addShotButton;
    private Button addSakeButton;

    private Button minusBeerButton;
    private Button minusWineButton;
    private Button minusChampagneButton;
    private Button minusCocktailButton;
    private Button minusShotButton;
    private Button minusSakeButton;

    //Drinks info
    private ImageView beerImage, wineImage, champagneImage, cocktailImage, shotImage, sakeImage;
    private TextView drinkInfo;

    // Bottom Buttons
    private Button seeListButton;
    private Button refreshButton;
    private Button quickHelpButton;

    // Drink Counters
    private static int beerCounter = 0;
    private static int wineCounter = 0;
    private static int champagneCounter = 0;
    private static int cocktailCounter = 0;
    private static int shotCounter = 0;
    private static int sakeCounter = 0;

    //Total calories
    private static int totalCalories = 0;
    private TextView caloriesTextView;

    // Drink Calories Mapping
    private static final Map<String, Integer> drinkCalories = new HashMap<>();

    //Firestore database
    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define calorie values per drink
        drinkCalories.put("Beer", 150);
        drinkCalories.put("Wine", 125);
        drinkCalories.put("Champagne", 90);
        drinkCalories.put("Cocktail", 200);
        drinkCalories.put("Shot", 95);
        drinkCalories.put("Sake", 230);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Quick Help button (TEMP!!!!)
        Button quickHelpButton = rootView.findViewById(R.id.quickHelpButton);

        quickHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Notify Emergency Contact Feature Still In Progress", Toast.LENGTH_SHORT).show();

            }
        });

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Initialize BAC section views
        bacLevel = view.findViewById(R.id.bacLevel);
        bacProgressBar = view.findViewById(R.id.bacProgressBar);
        bacStatus = view.findViewById(R.id.bacStatus);

        // Initialize alcohol counter views
        beerCount = view.findViewById(R.id.beerCount);
        wineCount = view.findViewById(R.id.wineCount);
        champagneCount = view.findViewById(R.id.champagneCount);
        cocktailCount = view.findViewById(R.id.cocktailCount);
        shotCount = view.findViewById(R.id.shotCount);
        sakeCount = view.findViewById(R.id.sakeCount);

        addBeerButton = view.findViewById(R.id.addBeerButton);
        addWineButton = view.findViewById(R.id.addWineButton);
        addChampagneButton = view.findViewById(R.id.addChampagneButton);
        addCocktailButton = view.findViewById(R.id.addCocktailButton);
        addShotButton = view.findViewById(R.id.addShotButton);
        addSakeButton = view.findViewById(R.id.addSakeButton);

        minusBeerButton = view.findViewById(R.id.minusBeerButton);
        minusWineButton = view.findViewById(R.id.minusWineButton);
        minusChampagneButton = view.findViewById(R.id.minusChampagneButton);
        minusCocktailButton = view.findViewById(R.id.minusCocktailButton);
        minusShotButton = view.findViewById(R.id.minusShotButton);
        minusSakeButton = view.findViewById(R.id.minusSakeButton);


        // Initialize action buttons
        seeListButton = view.findViewById(R.id.seeListButton);
        refreshButton = view.findViewById(R.id.refreshButton);
        quickHelpButton = view.findViewById(R.id.quickHelpButton);

        // Initialize ImageViews
        beerImage = view.findViewById(R.id.beerImage);
        wineImage = view.findViewById(R.id.wineImage);
        champagneImage = view.findViewById(R.id.champagneImage);
        cocktailImage = view.findViewById(R.id.cocktailImage);
        shotImage = view.findViewById(R.id.shotImage);
        sakeImage = view.findViewById(R.id.sakeImage);

        // Initialize TextView for displaying drink info
        drinkInfo = view.findViewById(R.id.drinkInfo);

        // Set click listeners for drink images
        beerImage.setOnClickListener(v -> displayDrinkInfo("Beer", 355, 0.03, 150));
        wineImage.setOnClickListener(v -> displayDrinkInfo("Wine", 150, 0.05, 125));
        champagneImage.setOnClickListener(v -> displayDrinkInfo("Champagne", 125, 0.04, 90));
        cocktailImage.setOnClickListener(v -> displayDrinkInfo("Cocktail", 200, 0.07, 200));
        shotImage.setOnClickListener(v -> displayDrinkInfo("Shot", 45, 0.04, 95));
        sakeImage.setOnClickListener(v -> displayDrinkInfo("Sake", 180, 0.06, 230));

        //Initialize total calories TextView
        caloriesTextView = view.findViewById(R.id.caloriesTextView);

        // Initialize firestore
        db = FirebaseFirestore.getInstance();

        // Define calorie values per drink
        drinkCalories.put("Beer", 150);
        drinkCalories.put("Wine", 125);
        drinkCalories.put("Champagne", 90);
        drinkCalories.put("Cocktail", 200);
        drinkCalories.put("Shot", 95);
        drinkCalories.put("Sake", 230);

        setupButtonListeners();

        // Handle arguments passed to the fragment (latest BAC)
        if (getArguments() != null) {
            String latestBacEntry = getArguments().getString("latest_bac_entry");
            if (latestBacEntry != null) {
                try {
                    double bacValue = Double.parseDouble(latestBacEntry);
                    updateBacLevel(bacValue);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid BAC entry from arguments: " + latestBacEntry, e);
                    showDefaultBacValue();
                }
            } else {
                showDefaultBacValue();
            }
        } else {
            showDefaultBacValue();

            //check for rapid logging and errors
            checkDrinkLogAndBAC();
        }

        // Initialize counters
        updateBeerCount();
        updateWineCount();
        updateChampagneCount();
        updateCocktailCount();
        updateShotCount();
        updateSakeCount();
        updateTotalCalories();


    }

    private void showDefaultBacValue() {
        bacLevel.setText("No Reading");
        bacProgressBar.setProgress(0);
        bacStatus.setText("MEASURE BAC to update!");
        bacStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bac_default));
    }

    private void setupButtonListeners() {

        seeListButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScanningActivity.class);
            startActivity(intent);
        });

        refreshButton.setOnClickListener(v -> {
            bacCheckEnabled = true;
            Intent intent = new Intent(getActivity(), ScanningActivity.class);
            intent.putExtra("mode", "refreshBAC");
            startActivity(intent);

        });

//        quickHelpButton.setOnClickListener(v -> {
//            // TODO: Implement quick help functionality
//            Log.d(TAG, "Quick Help button clicked");
//        });

        addBeerButton.setOnClickListener(v -> {
            beerCounter++;
            updateBeerCount();
            updateTotalCalories();
            //updateBACFromManualLogs();
            logDrinkToFirestore("Beer", 150, 0.03);

            //check for rapid logging and errors
            checkDrinkLogAndBAC();
        });

        addWineButton.setOnClickListener(v -> {
            wineCounter++;
            updateWineCount();
            updateTotalCalories();
            //updateBACFromManualLogs();
            logDrinkToFirestore("Wine", 125, 0.05);

            //check for rapid logging and errors
            checkDrinkLogAndBAC();
        });

        addChampagneButton.setOnClickListener(v -> {
            champagneCounter++;
            updateChampagneCount();
            updateTotalCalories();
            //updateBACFromManualLogs();
            logDrinkToFirestore("Champagne", 90, 0.04);

            //check for rapid logging and errors
            checkDrinkLogAndBAC();
        });

        addCocktailButton.setOnClickListener(v -> {
            cocktailCounter++;
            updateCocktailCount();
            updateTotalCalories();
            //updateBACFromManualLogs();
            logDrinkToFirestore("Cocktail", 200, 0.07);

            //check for rapid logging and errors
            checkDrinkLogAndBAC();
        });

        addShotButton.setOnClickListener(v -> {
            shotCounter++;
            updateShotCount();
            updateTotalCalories();
            //updateBACFromManualLogs();
            logDrinkToFirestore("Shot", 95, 0.04);

            //check for rapid logging and errors
            checkDrinkLogAndBAC();
        });

        addSakeButton.setOnClickListener(v -> {
            sakeCounter++;
            updateSakeCount();
            updateTotalCalories();
            //updateBACFromManualLogs();
            logDrinkToFirestore("Sake", 230, 0.06);

            //check for rapid logging and errors
            checkDrinkLogAndBAC();
        });

        minusBeerButton.setOnClickListener(v -> {
            if (beerCounter > 0) {
                beerCounter--;
                updateBeerCount();
                updateTotalCalories();
                removeDrinkFromFirestore("Beer");
                //updateBACFromManualLogs();
            }
        });

        minusWineButton.setOnClickListener(v -> {
            if (wineCounter > 0) {
                wineCounter--;
                updateWineCount();
                updateTotalCalories();
                removeDrinkFromFirestore("Wine");
                //updateBACFromManualLogs();
            }
        });

        minusChampagneButton.setOnClickListener(v -> {
            if (champagneCounter > 0) {
                champagneCounter--;
                updateChampagneCount();
                updateTotalCalories();
                removeDrinkFromFirestore("Champagne");
                //updateBACFromManualLogs();
            }
        });

        minusCocktailButton.setOnClickListener(v -> {
            if (cocktailCounter > 0) {
                cocktailCounter--;
                updateCocktailCount();
                updateTotalCalories();
                removeDrinkFromFirestore("Cocktail");
                //updateBACFromManualLogs();
            }
        });

        minusShotButton.setOnClickListener(v -> {
            if (shotCounter > 0) {
                shotCounter--;
                updateShotCount();
                updateTotalCalories();
                removeDrinkFromFirestore("Shot");
                //updateBACFromManualLogs();
            }
        });

        minusSakeButton.setOnClickListener(v -> {
            if (sakeCounter > 0) {
                sakeCounter--;
                updateSakeCount();
                updateTotalCalories();
                removeDrinkFromFirestore("Sake");
                //updateBACFromManualLogs();
            }
        });
    }

    /**
     * Retrieves the manual drink logs from Firestore, calculates the overall BAC using BACCalculator,
     * and updates the BAC display.
     */
    private void updateBACFromManualLogs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null) {
//            Log.e(TAG, "User not logged in; cannot update BAC");
//            return;
//        }
        String userId = user.getUid();
        db.collection("users")
                .document(userId)
                .collection("manual_drink_logs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DrinkLogItem> drinkLogs = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.getDocuments().forEach(doc -> {
                            DrinkLogItem logItem = doc.toObject(DrinkLogItem.class);
                            if (logItem != null) {
                                drinkLogs.add(logItem);
                            }
                        });
                    }
                    double estimatedBAC = BACCalculator.calculateBAC(drinkLogs);
                    updateBacLevel(estimatedBAC);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving manual drink logs", e);
                });
    }
    private void updateBeerCount() {
        beerCount.setText(String.valueOf(beerCounter));
    }

    private void updateWineCount() {
        wineCount.setText(String.valueOf(wineCounter));
    }

    private void updateChampagneCount() {
        champagneCount.setText(String.valueOf(champagneCounter));
    }

    private void updateCocktailCount() {
        cocktailCount.setText(String.valueOf(cocktailCounter));
    }

    private void updateShotCount() {
        shotCount.setText(String.valueOf(shotCounter));
    }

    private void updateSakeCount() {
        sakeCount.setText(String.valueOf(sakeCounter));
    }

    private void updateBacLevel(double bacValue) {
        if (getContext() == null) return;

        // Update BAC percentage text
        bacLevel.setText(String.format("%.2f%%", bacValue));

        // Convert to progress (assuming 100 is max BAC * 1.0)
        int progress = (int) (bacValue * 100);
        bacProgressBar.setProgress(progress);

        // Set status, text color, and progress drawable
        if (bacValue <= 0.02) {
            bacStatus.setText("Safe");
            bacStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bac_safe));
            bacProgressBar.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bac_progress_bar_safe));
        } else if (bacValue <= 0.05) {
            bacStatus.setText("Mild Impairment");
            bacStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bac_mild_impairment));
            bacProgressBar.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bac_progress_bar_mild_impairment));
        } else if (bacValue <= 0.08) {
            bacStatus.setText("Impaired");
            bacStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bac_impaired));
            bacProgressBar.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bac_progress_bar_impaired));
        } else if (bacValue <= 0.15) {
            bacStatus.setText("High Impairment");
            bacStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bac_high_impairment));
            bacProgressBar.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bac_progress_bar_high_impairment));
        } else if (bacValue <= 0.30) {
            bacStatus.setText("Severe Impairment");
            bacStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bac_severe_impairment));
            bacProgressBar.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bac_progress_bar_severe_impairment));
        } else {
            bacStatus.setText("Medical Emergency");
            bacStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bac_medical_emergency));
            bacProgressBar.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bac_progress_bar_medical_emergency));
        }

        Log.d(TAG, "BAC updated: " + bacValue + ", progress: " + progress);
    }

    private void displayDrinkInfo(String name, int volume, double bac, int calories) {
        if (drinkInfo.getVisibility() == View.VISIBLE) {
            drinkInfo.setVisibility(View.GONE); // Hide if already shown
        } else {
            // Format the name to be bold and underlined using HTML
            String formattedName = "<b><u>" + name + "</u></b>";

            // Multiply bac by 100 to show as a percentage and add a % symbol
            String info = String.format("%s<br>Volume: %dml<br>BAC: %.2f%%<br>Calories: %d kcal",
                    formattedName, volume, bac, calories);

            // Set the text using Html.fromHtml() to render the formatting
            drinkInfo.setText(Html.fromHtml(info));

            drinkInfo.setVisibility(View.VISIBLE);
        }
    }


    private void updateTotalCalories() {
        totalCalories = (beerCounter * drinkCalories.get("Beer")) +
                (wineCounter * drinkCalories.get("Wine")) +
                (champagneCounter * drinkCalories.get("Champagne")) +
                (cocktailCounter * drinkCalories.get("Cocktail")) +
                (shotCounter * drinkCalories.get("Shot")) +
                (sakeCounter * drinkCalories.get("Sake"));

        // Update UI
        caloriesTextView.setText("Total Calories: " + totalCalories + " kcal");
    }

    private void logDrinkToFirestore(String drinkType, int calories, double BACContribution) {

        // Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            // Get current timestamp
            Timestamp timestamp = new Timestamp(new Date());

            /*Before saving the new drink, query the last logged drink (if any), we will use it's timestamp
                to calculate time interval between last drink and new drink to determine if binge-drinking */
            db.collection("users").document(userId)
                    .collection("manual_drink_logs")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        final int[] interval = {0}; //default is 0 if first log
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot lastDrink = queryDocumentSnapshots.getDocuments().get(0);
                            Timestamp lastTimestamp = lastDrink.getTimestamp("timestamp");

                            if (lastTimestamp != null) {
                                long milliseconds = timestamp.toDate().getTime() - lastTimestamp.toDate().getTime();
                                interval[0] = (int) (milliseconds / 1000);  //Convert to seconds
                            }
                        }

                        // Store drink log as a HashMap
                        Map<String, Object> drinkEntry = new HashMap<>();
                        drinkEntry.put("drinkType", drinkType);
                        drinkEntry.put("calories", calories);
                        drinkEntry.put("timestamp", timestamp);
                        drinkEntry.put("BAC_Contribution", BACContribution);

                        // Save to Firestore inside "users/{userId}/manual_drink_logs"
                        db.collection("users").document(userId)
                                .collection("manual_drink_logs")
                                .add(drinkEntry)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("Firestore", "Drink logged for user: " + userId);

                                    //Generate a recommendation after a drink is logged
                                    db.collection("users").document(userId)
                                            .collection("manual_drink_logs")
                                            .get().addOnSuccessListener(drinkSnapshots -> {
                                                int drinkCount = drinkSnapshots.size(); //Fetch total drinks logged
                                                //create recommendation
                                                Recommendation recommendation = new Recommendation(drinkCount, interval[0], Timestamp.now());
                                                //Store the recommendation
                                                storeRecommendation(recommendation);
                                                //Show the recommendation pop-up
                                                showRecommendationDialog(drinkCount, recommendation.getMessage());
                                            })
                                            .addOnFailureListener(e -> Log.e("Firestore", "Error fetching drink logs", e));

                                })
                                .addOnFailureListener(e ->
                                        Log.e("Firestore", "Error adding drink log", e));
                    });
        } else {
            Log.e("Firestore", "User not logged in, cannot save drink log");
        }
    }

    private void removeDrinkFromFirestore(String drinkType){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            String userID = user.getUid();

            db.collection("users")
                    .document(userID)
                    .collection("manual_drink_logs")
                    .whereEqualTo("drinkType", drinkType)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        DocumentSnapshot drinkEntry = queryDocumentSnapshots.getDocuments().get(0);
                        drinkEntry.getReference().delete()
                                .addOnSuccessListener(result ->{
                                    Log.d("Firestore", drinkType + "successfully deleted");
                                })
                                .addOnFailureListener(error ->{
                                    Log.d("Firestore", "Error deleting entry" + error);
                                });
                    });
        }
    }

    //Store the recommendation to firestore
    public void storeRecommendation(Recommendation recommendation) {
        db = FirebaseFirestore.getInstance();

        Map<String,Object> recommendationMap = new HashMap<>();
        recommendationMap.put("DrinkCount", recommendation.getDrinkCount());
        recommendationMap.put("Message", recommendation.getMessage());
        recommendationMap.put("Timestamp", Timestamp.now());
        recommendationMap.put("Resolved", recommendation.isResolved());

        String userId = getCurrentUserId();
        DocumentReference documentReference = db.collection("users")
                .document(userId)
                .collection("Recommendations")
                .document(recommendation.getDate() + " " + recommendation.getTime());

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                documentReference.update(recommendationMap);
                Log.d("Firestore", "Recommendation entry updated successfully");
            } else {
                documentReference.set(recommendationMap);
                Log.d("Firestore", "Recommendation entry saved successfully");
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error: "+e);
        });
    }







    //TODO: test if it is annoying that the drinks are checked everytime bac updated
    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;
        Log.d(TAG, "Current user ID: " + userId);
        return userId;
    }


    private static int rapidLoggingCount = 0; // Tracks repeated alerts in a session

    private void checkDrinkLogAndBAC() {
        Log.d(TAG, "Loading BAC history");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = getCurrentUserId();

        if (userId == null) {
            Log.e(TAG, "No user is signed in");
            return;
        }

        // Track if alert for 1 or 2 has already been triggered
        AtomicBoolean alertTriggered = new AtomicBoolean(false);

        //1. Rapid Drink Logging (soft rec):
        long tenMinutesAgo = System.currentTimeMillis() - (10 * 60 * 1000); // 10 minutes ago
        Date tenMinutesAgoDate = new Date(tenMinutesAgo); // Convert long to Date
        Timestamp timestamp = new Timestamp(tenMinutesAgoDate); // Convert Date to Timestamp

        db.collection("users")
                .document(userId)
                .collection("manual_drink_logs")
                .whereGreaterThan("timestamp", timestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Check if we have 5 or more drinks in the past 10 minutes
                    if (queryDocumentSnapshots.size() >= 5) {
                        // Log a message indicating that 5 drinks were logged
                        Log.d(TAG, "You have logged 5 drinks in the past 10 minutes. If this was a mistake, use the minus buttons to decrease the count.");
                        String title1 = "Too Many Drinks Logged";
                        String message1;

                        switch (rapidLoggingCount) {
                            case 1:
                                message1 = "You logged 5 drinks in a short time. Was this a mistake?";
                                break;
                            case 2:
                                message1 = "You've logged drinks rapidly multiple times. Be mindful of your drinking pace.";
                                break;
                            case 3:
                                message1 = "This is your third time logging drinks too quickly. Please slow down for your safety.";
                                break;
                            case 4:
                                message1 = "Excessive rapid drinking detected. Consider taking a break.";
                                break;
                            default:
                                message1 = "Severe warning: Drinking at this pace can be dangerous. Seek help if needed.";
                                break;
                        }

                        if (!alertTriggered.get()) {
                            showAlertWithUndo(
                                    title1,
                                    message1
                            );
                        }
//


                        // Iterate over the logged drinks and log each timestamp
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Timestamp drinkTimestamp = document.getTimestamp("timestamp");
                            if (drinkTimestamp != null) {
                                Log.d(TAG, "Timestamp of the logged drink: " + drinkTimestamp.toDate());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking rapid drink logs", e));

        //2. Unusually Large Drink Entries (hard rec):
        long oneMinuteAgo = System.currentTimeMillis() - (60 * 1000); // 1 minute ago
        Date oneMinuteAgoDate = new Date(oneMinuteAgo); // Convert long to Date
        Timestamp timestamp2 = new Timestamp(oneMinuteAgoDate); // Convert Date to Timestamp

        db.collection("users")
                .document(userId)
                .collection("manual_drink_logs")
                .whereGreaterThan("timestamp", timestamp2)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // If 5 or more drinks were logged in the last minute
                    if (queryDocumentSnapshots.size() >= 5) {
                        Log.d(TAG, "You have logged 5 drinks in the past 1 minute. Would you like to undo these changes?");
                        showAlertWithUndo(
                                "Too Many Drinks Logged",
                                "You logged 5 drinks in 1 minute. Was this a mistake?"
                        );
                        alertTriggered.set(true);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking unusually large drink entries", e));


        //3. Rapid BAC Changes and
        //4. Unusually High BAC
        db.collection("users")
                .document(userId)
                .collection("BacEntry")
                .orderBy("Date")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double previousBAC = -1;
                    long previousTimestamp = -1;
                    double spikeThreshold = 0.05;  // BAC increase threshold
                    long timeWindow = 10 * 60 * 1000;  // 10 minutes in milliseconds

                    double unusuallyHighBACThreshold = 0.45;  // Unusually high BAC threshold


                    // Get the current date and start of today
                    long todayStartTimestamp = getStartOfTodayTimestamp();
                    long currentTimestamp = System.currentTimeMillis();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        // Retrieve the Date and BAC value
                        String dateString = document.getString("Date");
                        Double bac = document.getDouble("bacValue");

                        if (bac != null && dateString != null) {
                            try {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                Date date = dateFormat.parse(dateString);

                                // Get the timestamp of the current entry
                                long entryTimestamp = date.getTime();

                                // Only consider entries from today
                                if (entryTimestamp >= todayStartTimestamp) {

                                    if (bac > unusuallyHighBACThreshold) {
                                        String message = "Your BAC is EXTREMELY high. Please measure your BAC again to confirm the reading.";
                                        Log.d(TAG, "Unusually High BAC Detected: " + message);
                                        showAlert(
                                                "Unusually High BAC Detected",
                                                message
                                        );

                                    }

                                    // Check for rapid BAC change within the 10-minute window
                                    if (previousBAC != -1 && entryTimestamp - previousTimestamp <= timeWindow) {
                                        // Log.d(TAG, String.valueOf(bac));
                                        // Log.d(TAG, String.valueOf(previousBAC));

                                        double bacDifference = bac - previousBAC;
                                        if (bacDifference >= spikeThreshold) {
                                            String message = "Your BAC has increased by " + bacDifference + " in the past 10 minutes. Please check again to make sure.";
                                            Log.d(TAG, "Rapid BAC Change Detected: " + message);

                                            showAlert(
                                                    "Rapid BAC Change Detected",
                                                    message
                                            );
                                        }
                                    }

                                    // Update previousBAC and previousTimestamp for next iteration
                                    previousBAC = bac;
                                    previousTimestamp = entryTimestamp;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing date: " + e.getMessage());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking BAC entries", e));
    }

    private long getStartOfTodayTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private final Map<String, Long> alertCooldowns = new HashMap<>();
    private static final long ALERT_COOLDOWN_PERIOD = 5 * 60 * 1000; // 5 minutes
    private boolean bacCheckEnabled = false; // Flag to enable BAC checks


    private void showAlertWithUndo(String title, String message) {
        if (!canShowDrinkAlert(title)) return; // Check cooldown


        // Create dialog with custom theme
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.RedBorderAlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (d, which) -> Log.d(TAG, "OK clicked"))
                .setNegativeButton("UNDO", (d, which) -> Log.d(TAG, "Undo clicked"))
                .create();

        playNotificationSound();

        // Show before styling buttons
        dialog.show();

        // Customize buttons
        int darkRed = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark);
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(darkRed);
            positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }

        if (negativeButton != null) {
            negativeButton.setTextColor(darkRed);
            negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }

        // Make title and message larger
        TextView titleView = dialog.findViewById(android.R.id.title);
        TextView messageView = dialog.findViewById(android.R.id.message);

        if (titleView != null) {
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20); // Title size
            titleView.setTextColor(darkRed); // Optional: make title red too
        }

        if (messageView != null) {
            messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Message size
        }
    }

    private boolean canShowDrinkAlert(String title) {
        long currentTime = System.currentTimeMillis();
        Long lastShownTime = alertCooldowns.get(title);

        if (lastShownTime == null || (currentTime - lastShownTime) > ALERT_COOLDOWN_PERIOD) {
            return true; // Show alert if not shown before or cooldown expired
        }

        Log.d(TAG, "Alert '" + title + "' is still in cooldown.");
        return false;
    }

    private boolean canShowBACAlert(String title) {
        if (bacCheckEnabled) {
            return true; // Show alert if new bac logged
        }

        Log.d(TAG, "Alert '" + title + "' is still in cooldown.");
        return false;
    }
    private void showAlert(String title, String message) {
        if (!canShowBACAlert(title)) return; // Check cooldown

        // Create dialog with the same custom theme
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.RedBorderAlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (d, which) -> Log.d(TAG, "OK clicked"))
                .create();

        // Play the same notification sound
        playNotificationSound();

        // Show before styling
        dialog.show();

        // Customize button (same style as Undo version)
        int darkRed = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark);
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(darkRed);
            positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Same 18sp size
        }

        // Identical title/message styling
        TextView titleView = dialog.findViewById(android.R.id.title);
        TextView messageView = dialog.findViewById(android.R.id.message);

        if (titleView != null) {
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20); // Same 20sp title
            titleView.setTextColor(darkRed); // Same red title
        }

        if (messageView != null) {
            messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Same 18sp message
        }

        bacCheckEnabled = false;
    }

//    private void playNotificationSound() {
//        try {
//            MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alert2);
//            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
//            mediaPlayer.start();
//        } catch (Exception e) {
//            Log.e(TAG, "Error playing sound", e);
//        }
//    }

    private void playNotificationSound() {
        RingtoneManager.getRingtone(requireContext(),
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .play();
    }


    //Recommendation pop-up
    private void showRecommendationDialog(int drinkCount, String message) {
        String title = "Healthy tips!";
        if(!canShowDrinkAlert(title)) return;

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.RecommendationDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (d, which) -> Log.d(TAG, "OK clicked"))
                .create();

        //DOES IT NEED A SOUND? playNotificationSound();

        dialog.show();

        //Buttons
        int greenColor =  ContextCompat.getColor(requireContext(),android.R.color.holo_green_dark);
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        if(positiveButton != null) {
            positiveButton.setTextColor(greenColor);
            positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }

        //Title and message
        TextView titleView = dialog.findViewById(android.R.id.title);
        TextView messageView = dialog.findViewById(android.R.id.message);

        if(titleView != null) {
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            titleView.setTextColor(greenColor);
        }

        if(messageView != null) {
            messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }

    }

}