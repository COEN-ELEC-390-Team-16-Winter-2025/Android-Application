package com.drinkwise.app.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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
import com.drinkwise.app.ScanningActivity;
import com.drinkwise.app.ui.home.drinklog.BACCalculator;
import com.drinkwise.app.ui.home.drinklog.DrinkLogItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
        });

        addWineButton.setOnClickListener(v -> {
            wineCounter++;
            updateWineCount();
            updateTotalCalories();
            //updateBACFromManualLogs();
            logDrinkToFirestore("Wine", 125, 0.05);
        });

        addChampagneButton.setOnClickListener(v -> {
            champagneCounter++;
            updateChampagneCount();
            updateTotalCalories();
            //updateBACFromManualLogs();
            logDrinkToFirestore("Champagne", 90, 0.04);
        });

        addCocktailButton.setOnClickListener(v -> {
            cocktailCounter++;
            updateCocktailCount();
            updateTotalCalories();
            //updateBACFromManualLogs();
            logDrinkToFirestore("Cocktail", 200, 0.07);
        });

        addShotButton.setOnClickListener(v -> {
            shotCounter++;
            updateShotCount();
            updateTotalCalories();
            //updateBACFromManualLogs();
            logDrinkToFirestore("Shot", 95, 0.04);
        });

        addSakeButton.setOnClickListener(v -> {
            sakeCounter++;
            updateSakeCount();
            updateTotalCalories();
            //updateBACFromManualLogs();
            logDrinkToFirestore("Sake", 230, 0.06);
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
                    .addOnSuccessListener(documentReference ->
                            Log.d("Firestore", "Drink logged for user: " + userId))
                    .addOnFailureListener(e ->
                            Log.e("Firestore", "Error adding drink log", e));
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
}