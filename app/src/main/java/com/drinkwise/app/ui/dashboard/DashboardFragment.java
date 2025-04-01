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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.drinkwise.app.SettingsActivity;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class DashboardFragment extends Fragment {
    private static final String TAG = "DashboardFragment";

    // Top Section Views
    private TextView bacLevel;
    private ProgressBar bacProgressBar;
    private TextView bacStatus;

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

    // Drinks info
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

    // Quick Help Related variables
    private static int quickHelpCounter = 0;
    private double bacValue = 0;

    // Preferences Related variables
    private boolean notifications, alerts, reminders, quickHelp;

    // Total calories
    private static int totalCalories = 0;
    private TextView caloriesTextView;

    // Drink Calories Mapping
    private static final Map<String, Integer> drinkCalories = new HashMap<>();

    // Firestore database
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
        quickHelpButton = rootView.findViewById(R.id.quickHelpButton);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views (omitted for brevity, same as your current code)
        bacLevel = view.findViewById(R.id.bacLevel);
        bacProgressBar = view.findViewById(R.id.bacProgressBar);
        bacStatus = view.findViewById(R.id.bacStatus);
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

        seeListButton = view.findViewById(R.id.seeListButton);
        refreshButton = view.findViewById(R.id.refreshButton);
        quickHelpButton = view.findViewById(R.id.quickHelpButton);

        beerImage = view.findViewById(R.id.beerImage);
        wineImage = view.findViewById(R.id.wineImage);
        champagneImage = view.findViewById(R.id.champagneImage);
        cocktailImage = view.findViewById(R.id.cocktailImage);
        shotImage = view.findViewById(R.id.shotImage);
        sakeImage = view.findViewById(R.id.sakeImage);

        drinkInfo = view.findViewById(R.id.drinkInfo);
        caloriesTextView = view.findViewById(R.id.caloriesTextView);

        db = FirebaseFirestore.getInstance();

        // Setup additional listeners and preferences
        fetchPreferences((notifications, alerts, reminders, quickHelp) -> {
            displayQuickHelp(quickHelp);
        });
        setupButtonListeners();

        // Handle arguments and default BAC
        if (getArguments() != null) {
            String latestBacEntry = getArguments().getString("latest_bac_entry");
            if (latestBacEntry != null) {
                try {
                    bacValue = Double.parseDouble(latestBacEntry);
                    updateBacLevel(bacValue);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid BAC entry from arguments: " + latestBacEntry, e);
                    showDefaultBacValue();
                }
            } else {
                showDefaultBacValue();
                checkDrinkLogAndBAC();
            }
        } else {
            showDefaultBacValue();
            checkDrinkLogAndBAC();
        }

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

        quickHelpButton.setOnClickListener(v -> {
            quickHelpCounter++;
            Log.d(TAG, "Quick help button clicked. Count: " + quickHelpCounter);
        });

        addBeerButton.setOnClickListener(v -> {
            beerCounter++;
            updateBeerCount();
            updateTotalCalories();
            logDrinkToFirestore("Beer", 150, 0.03);
            checkDrinkLogAndBAC();
        });

        addWineButton.setOnClickListener(v -> {
            wineCounter++;
            updateWineCount();
            updateTotalCalories();
            logDrinkToFirestore("Wine", 125, 0.05);
            checkDrinkLogAndBAC();
        });

        addChampagneButton.setOnClickListener(v -> {
            champagneCounter++;
            updateChampagneCount();
            updateTotalCalories();
            logDrinkToFirestore("Champagne", 90, 0.04);
            checkDrinkLogAndBAC();
        });

        addCocktailButton.setOnClickListener(v -> {
            cocktailCounter++;
            updateCocktailCount();
            updateTotalCalories();
            logDrinkToFirestore("Cocktail", 200, 0.07);
            checkDrinkLogAndBAC();
        });

        addShotButton.setOnClickListener(v -> {
            shotCounter++;
            updateShotCount();
            updateTotalCalories();
            logDrinkToFirestore("Shot", 95, 0.04);
            checkDrinkLogAndBAC();
        });

        addSakeButton.setOnClickListener(v -> {
            sakeCounter++;
            updateSakeCount();
            updateTotalCalories();
            logDrinkToFirestore("Sake", 230, 0.06);
            checkDrinkLogAndBAC();
        });

        // Similarly add listeners for minus buttons with logs if needed...
    }

    /**
     * Retrieves manual drink logs from Firestore, calculates overall BAC, and updates the display.
     */
    private void updateBACFromManualLogs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User not logged in; cannot update BAC");
            return;
        }
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
                    Log.d(TAG, "Calculated BAC from manual logs: " + estimatedBAC);
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

        bacLevel.setText(String.format("%.2f%%", bacValue));
        int progress = (int) (bacValue * 100);
        bacProgressBar.setProgress(progress);

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

        displayQuickHelp(quickHelp);
        Log.d(TAG, "BAC updated: " + bacValue + ", progress: " + progress);
    }

    private void displayDrinkInfo(String name, int volume, double bac, int calories) {
        if (drinkInfo.getVisibility() == View.VISIBLE) {
            drinkInfo.setVisibility(View.GONE);
        } else {
            String formattedName = "<b><u>" + name + "</u></b>";
            String info = String.format("%s<br>Volume: %dml<br>BAC: %.2f%%<br>Calories: %d kcal",
                    formattedName, volume, bac, calories);
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
        caloriesTextView.setText("Total Calories: " + totalCalories + " kcal");
    }

    int drinkCount;
    private void logDrinkToFirestore(String drinkType, int calories, double BACContribution) {
        Log.d(TAG, "Starting logDrinkToFirestore for drink: " + drinkType);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User ID is null, cannot create recommendation.");
            return;
        }
        String userId = user.getUid();
        Timestamp timestamp = new Timestamp(new Date());

        db.collection("users").document(userId)
                .collection("manual_drink_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final int[] interval = {0};
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot lastDrink = queryDocumentSnapshots.getDocuments().get(0);
                        Timestamp lastTimestamp = lastDrink.getTimestamp("timestamp");
                        if (lastTimestamp != null) {
                            long milliseconds = timestamp.toDate().getTime() - lastTimestamp.toDate().getTime();
                            interval[0] = (int) (milliseconds / 1000);
                            Log.d(TAG, "Time interval since last drink: " + interval[0] + " seconds");
                            if (interval[0] > 7200) {
                                Log.d(TAG, "Interval > 2 hours, prompting new session.");
                                askUserToStartNewSession(userId);
                                return;
                            }
                        }
                    }

                    db.collection("users").document(userId)
                            .collection("drinking_sessions")
                            .orderBy("startTimestamp", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(sessionSnapshots -> {
                                String sessionId;
                                if (!sessionSnapshots.isEmpty()) {
                                    DocumentSnapshot lastSession = sessionSnapshots.getDocuments().get(0);
                                    Timestamp lastDrinkTime = lastSession.getTimestamp("lastDrinkTimestamp");
                                    if (lastDrinkTime != null &&
                                            (new Timestamp(new Date()).toDate().getTime() - lastDrinkTime.toDate().getTime()) > 7200000) {
                                        sessionId = db.collection("users").document(userId)
                                                .collection("drinking_sessions").document().getId();
                                        startNewSession(userId, sessionId);
                                    } else {
                                        sessionId = lastSession.getId();
                                    }
                                } else {
                                    sessionId = db.collection("users").document(userId)
                                            .collection("drinking_sessions").document().getId();
                                    startNewSession(userId, sessionId);
                                }

                                db.collection("users").document(userId)
                                        .collection("manual_drink_logs")
                                        .whereEqualTo("sessionId", sessionId)
                                        .get()
                                        .addOnSuccessListener(drinkSnapshots -> {
                                            drinkCount = drinkSnapshots.size() + 1;
                                            Log.d(TAG, "Total drink count for session: " + drinkCount);

                                            Map<String, Object> drinkEntry = new HashMap<>();
                                            drinkEntry.put("drinkType", drinkType);
                                            drinkEntry.put("calories", calories);
                                            drinkEntry.put("timestamp", timestamp);
                                            drinkEntry.put("BAC_Contribution", BACContribution);
                                            drinkEntry.put("sessionId", sessionId);

                                            db.collection("users").document(userId)
                                                    .collection("manual_drink_logs")
                                                    .add(drinkEntry)
                                                    .addOnSuccessListener(documentReference -> {
                                                        Log.d(TAG, "Drink logged for user: " + userId + ", Document ID: " + documentReference.getId());

                                                        // Create recommendation
                                                        Recommendation recommendation = new Recommendation(drinkCount, interval[0], Timestamp.now());
                                                        Log.d(TAG, "Created recommendation: " + recommendation.getMessage());

                                                        // Store recommendation and then show pop-up
                                                        storeRecommendation(recommendation);
                                                        showRecommendationDialog(drinkCount, recommendation.getMessage());
                                                    })
                                                    .addOnFailureListener(e -> Log.e(TAG, "Error adding drink log", e));
                                        });
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error fetching drinking session", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching last drink log", e));
    }

    private void removeDrinkFromFirestore(String drinkType) {
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
                                .addOnSuccessListener(result -> {
                                    Log.d(TAG, drinkType + " successfully deleted");
                                })
                                .addOnFailureListener(error -> {
                                    Log.d(TAG, "Error deleting entry: " + error);
                                });
                    });
        }
    }

    private void askUserToStartNewSession(String userId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("New Drinking Session")
                .setMessage("More than 2 hours since your last drink. Start a new session?")
                .setPositiveButton("Yes", (d, which) -> {
                    String newSessionId = db.collection("users").document(userId)
                            .collection("drinking_sessions").document().getId();
                    startNewSession(userId, newSessionId);
                })
                .setNegativeButton("No", (d, which) -> d.dismiss())
                .show();
    }

    private void startNewSession(String userId, String sessionId) {
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("startTimestamp", new Timestamp(new Date()));
        sessionData.put("sessionId", sessionId);

        db.collection("users").document(userId)
                .collection("drinking_sessions")
                .document(sessionId)
                .set(sessionData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "New drinking session started: " + sessionId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to start a new drinking session", e));
    }

    // Store recommendation to Firestore with added logs.
    public void storeRecommendation(Recommendation recommendation) {
        Log.d(TAG, "Storing recommendation: " + recommendation.getMessage());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User is not authenticated");
            return;
        }
        String userId = user.getUid();
        Map<String, Object> recommendationMap = new HashMap<>();
        recommendationMap.put("DrinkCount", recommendation.getDrinkCount());
        recommendationMap.put("Message", recommendation.getMessage());
        recommendationMap.put("Timestamp", Timestamp.now());
        recommendationMap.put("Resolved", recommendation.isResolved());

        db.collection("users")
                .document(userId)
                .collection("Recommendations")
                .add(recommendationMap)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Recommendation saved successfully with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving recommendation: " + e);
                });
    }

    // List for undo logs
    List<String> drinkLogToUndo = new ArrayList<>();

    private void checkDrinkLogAndBAC() {
        Log.d(TAG, "checkDrinkLogAndBAC: Starting BAC history check");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "No user is signed in");
            return;
        }

        AtomicBoolean alertTriggered = new AtomicBoolean(false);
        long tenMinutesAgo = System.currentTimeMillis() - (10 * 60 * 1000);
        Date tenMinutesAgoDate = new Date(tenMinutesAgo);
        Timestamp timestamp = new Timestamp(tenMinutesAgoDate);

        db.collection("users")
                .document(userId)
                .collection("manual_drink_logs")
                .whereGreaterThan("timestamp", timestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() >= 5) {
                        Log.d(TAG, "Rapid logging detected: " + queryDocumentSnapshots.size() + " drinks in past 10 minutes.");
                        String title1 = "Too Many Drinks Logged";
                        String message1;
                        switch (quickHelpCounter) {
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
                            showAlertWithUndo(title1, message1);
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Timestamp drinkTimestamp = document.getTimestamp("timestamp");
                            if (drinkTimestamp != null) {
                                Log.d(TAG, "Rapid log timestamp: " + drinkTimestamp.toDate());
                            }
                            drinkLogToUndo.add(document.getId());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking rapid drink logs", e));

        // Check for unusually large drink entries in 1 minute
        long oneMinuteAgo = System.currentTimeMillis() - (60 * 1000);
        Date oneMinuteAgoDate = new Date(oneMinuteAgo);
        Timestamp timestamp2 = new Timestamp(oneMinuteAgoDate);
        db.collection("users")
                .document(userId)
                .collection("manual_drink_logs")
                .whereGreaterThan("timestamp", timestamp2)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() >= 5) {
                        Log.d(TAG, "Unusually large drink entries detected: " + queryDocumentSnapshots.size() + " drinks in 1 minute.");
                        showAlertWithUndo("Too Many Drinks Logged", "You logged 5 drinks in 1 minute. Was this a mistake?");
                        alertTriggered.set(true);
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Timestamp drinkTimestamp = document.getTimestamp("timestamp");
                            if (drinkTimestamp != null) {
                                Log.d(TAG, "Unusually large log timestamp: " + drinkTimestamp.toDate());
                            }
                            drinkLogToUndo.add(document.getId());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking unusually large drink entries", e));

        // Additional checks for rapid BAC changes and unusually high BAC (omitted for brevity)
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
    private static final long ALERT_COOLDOWN_PERIOD = 5 * 60 * 1000;
    private boolean bacCheckEnabled = false;

    private void showAlertWithUndo(String title, String message) {
        if (!canShowDrinkAlert(title)) {
            Log.d(TAG, "showAlertWithUndo: Cooldown active for alert: " + title);
            return;
        }
        Log.d(TAG, "showAlertWithUndo: Showing alert with title: " + title);
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.RedBorderAlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (d, which) -> {
                    Log.d(TAG, "Alert OK clicked");
                    quickHelpCounter++;
                })
                .setNegativeButton("UNDO", (d, which) -> {
                    Log.d(TAG, "Alert UNDO clicked");
                    quickHelpCounter--;
                    deleteLogs(drinkLogToUndo);
                })
                .create();
        playNotificationSound();
        dialog.show();
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
        TextView titleView = dialog.findViewById(android.R.id.title);
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (titleView != null) {
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            titleView.setTextColor(darkRed);
        }
        if (messageView != null) {
            messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }
    }

    private void deleteLogs(List<String> drinkLogToUndo) {
        Log.d(TAG, "Deleting logs: " + drinkLogToUndo.toString());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (String logId : drinkLogToUndo) {
            db.collection("users")
                    .document(getCurrentUserId())
                    .collection("manual_drink_logs")
                    .document(logId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String drinkType = documentSnapshot.getString("drinkType");
                        if (drinkType != null) {
                            switch (drinkType) {
                                case "Beer":
                                    if (beerCounter > 0) beerCounter--;
                                    updateBeerCount();
                                    totalCalories -= 150;
                                    break;
                                case "Wine":
                                    if (wineCounter > 0) wineCounter--;
                                    updateWineCount();
                                    totalCalories -= 125;
                                    break;
                                case "Champagne":
                                    if (champagneCounter > 0) champagneCounter--;
                                    updateChampagneCount();
                                    totalCalories -= 90;
                                    break;
                                case "Cocktail":
                                    if (cocktailCounter > 0) cocktailCounter--;
                                    updateCocktailCount();
                                    totalCalories -= 200;
                                    break;
                                case "Shot":
                                    if (shotCounter > 0) shotCounter--;
                                    updateShotCount();
                                    totalCalories -= 95;
                                    break;
                                case "Sake":
                                    if (sakeCounter > 0) sakeCounter--;
                                    updateSakeCount();
                                    totalCalories -= 250;
                                    break;
                                default:
                                    Log.e(TAG, "Unknown drink type: " + drinkType);
                            }
                            updateTotalCalories();
                            db.collection("users")
                                    .document(getCurrentUserId())
                                    .collection("manual_drink_logs")
                                    .document(logId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Log " + logId + " successfully deleted"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error deleting log " + logId, e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error retrieving drink log " + logId, e));
        }
    }

    private boolean canShowDrinkAlert(String title) {
        long currentTime = System.currentTimeMillis();
        Long lastShownTime = alertCooldowns.get(title);
        if (lastShownTime == null || (currentTime - lastShownTime) > ALERT_COOLDOWN_PERIOD) {
            alertCooldowns.put(title, currentTime);
            return true;
        }
        Log.d(TAG, "Alert '" + title + "' is in cooldown.");
        return false;
    }

    private boolean canShowBACAlert(String title) {
        if (bacCheckEnabled) {
            return true;
        }
        Log.d(TAG, "BAC Alert '" + title + "' is in cooldown.");
        return false;
    }

    private void showAlert(String title, String message) {
        if (!canShowBACAlert(title)) return;
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.RedBorderAlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (d, which) -> Log.d(TAG, "Alert OK clicked"))
                .create();
        playNotificationSound();
        dialog.show();
        int darkRed = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark);
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(darkRed);
            positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }
        TextView titleView = dialog.findViewById(android.R.id.title);
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (titleView != null) {
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            titleView.setTextColor(darkRed);
        }
        if (messageView != null) {
            messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }
        bacCheckEnabled = false;
    }

    private void playNotificationSound() {
        RingtoneManager.getRingtone(requireContext(),
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .play();
    }

    // Recommendation pop-up with extra logging
    private void showRecommendationDialog(int drinkCount, String message) {
        String title = "Healthy tips!";
        Log.d(TAG, "Preparing to show recommendation dialog. Drink count: " + drinkCount + ", Message: " + message);
        if (!canShowDrinkAlert(title)) {
            Log.d(TAG, "Recommendation dialog not shown due to alert cooldown.");
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.RecommendationDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (d, which) -> Log.d(TAG, "Recommendation dialog OK clicked"))
                .create();
        // Uncomment if you want a sound here
        // playNotificationSound();
        dialog.show();
        int greenColor = ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark);
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(greenColor);
            positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }
        TextView titleView = dialog.findViewById(android.R.id.title);
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (titleView != null) {
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            titleView.setTextColor(greenColor);
        }
        if (messageView != null) {
            messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }
        Log.d(TAG, "Recommendation dialog displayed.");
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;
        Log.d(TAG, "Current user ID: " + userId);
        return userId;
    }

    public void fetchPreferences(SettingsActivity.PreferencesCallback callback) {
        db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(getCurrentUserId())
                .collection("profile")
                .document("Preferences")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.d(TAG, "Error fetching preferences: " + error.getMessage());
                    }
                    if (value != null && value.exists()) {
                        notifications = value.getBoolean("Notifications");
                        alerts = value.getBoolean("Alerts");
                        reminders = value.getBoolean("Reminders");
                        quickHelp = value.getBoolean("Quick_help");
                        Log.d(TAG, "Fetched Preferences - Notifications: " + notifications + ", Alerts: " + alerts +
                                ", Reminders: " + reminders + ", Quick Help: " + quickHelp);
                        callback.onCallback(notifications, alerts, reminders, quickHelp);
                    } else {
                        notifications = false;
                        alerts = false;
                        reminders = false;
                        quickHelp = false;
                        callback.onCallback(notifications, alerts, reminders, quickHelp);
                    }
                });
    }

    public void displayQuickHelp(boolean quickHelp) {
        if (!quickHelp) {
            quickHelpButton.setVisibility(View.VISIBLE);
        } else {
            if (bacValue < 0.05) {
                quickHelpButton.setVisibility(View.GONE);
            } else {
                quickHelpButton.setVisibility(View.VISIBLE);
            }
        }
        switch (bacStatus.getText().toString()) {
            case "High Impairment":
            case "Severe Impairment":
            case "Medical Emergency":
                Animation animation = AnimationUtils.loadAnimation(requireContext(), R.anim.button_pulse);
                quickHelpButton.startAnimation(animation);
                break;
        }
    }

    public interface PreferencesCallback {
        void onCallback(boolean notifications, boolean alerts, boolean reminders, boolean quickHelp);
    }
}
