package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;


import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;



public class ScanningActivity extends AppCompatActivity {

    private static final String TAG = "ScanningActivity";
    private static final String BLUNO_NAME = "Bluno";
    private static final UUID BLUNO_SERVICE_UUID = UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");
    private static final UUID BLUNO_CHARACTERISTIC_UUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    private static final UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter bluetoothAdapter;
    private Handler handler;
    private TextView loadingTextView;
    private ProgressBar progressBar;

    private boolean MODE_LATEST_BAC;
    String[] loading = {".", "..", "..."};

    private double bac_readings = 0.0;
    private ArrayList<Double> readings = new ArrayList<>();
    private int count = 0;
    private static int entry_count = 0;
    private ArrayAdapter<String> bacListAdapter;
    private ScanningAdapter adapter;
    private final List<String> bacList = new ArrayList<>();
    private ArrayList<BACEntry> bacEntries = new ArrayList<>();
    private BluetoothGatt mBluetoothGatt;

    private FirebaseFirestore db;
    private int userHeight = 170;  // Default height (cm)
    private int userWeight = 70;   // Default weight (kg)
    private String userId = "USER_ID_HERE"; // Replace with actual user ID

    private String lastStatus = null;
    private long lastStatusTime = System.currentTimeMillis();
    private int dangerCount = 0;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        String mode = getIntent().getStringExtra("mode");

        //setTitle("BAC Readings");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.custom_actionbar_title);
            TextView title = actionBar.getCustomView().findViewById(R.id.action_bar_title);
            title.setText("BAC Readings");
        }




        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            fetchUserData();
            startFirestoreSafetyListener();
        } else {
            Log.e(TAG, "No logged-in user found.");
        }


        ListView bacListView = findViewById(R.id.bacListView);
        loadingTextView = findViewById(R.id.loadingTextView);
        progressBar = findViewById(R.id.progressBar);
        TextView instructionTextView = findViewById(R.id.instructionTextView);

        handler = new Handler(Looper.getMainLooper());

        // Set up the ListView adapter
        //bacListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bacList);
        adapter = new ScanningAdapter(this, bacEntries);
        bacListView.setAdapter(adapter);


        // Initialize Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported");
            return;
        }

        // Hide the list if we are in refresh mode
        if ("refreshBAC".equals(mode)) {
            bacListView.setVisibility(View.GONE);
            loadingTextView.setVisibility(View.VISIBLE);
            instructionTextView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            MODE_LATEST_BAC = true;

        }



        // Connect to the Bluno device
        connectToBluno();
    }

    @SuppressLint("MissingPermission")
    private void connectToBluno() {
        BluetoothDevice device = getPairedBlunoDevice();
        if (device != null) {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        } else {
            Log.e(TAG, "No paired Bluno device found.");
        }
    }

    @SuppressLint("MissingPermission")
    private BluetoothDevice getPairedBlunoDevice() {
        if (bluetoothAdapter != null) {
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                if (device.getName().contains(BLUNO_NAME)) {
                    return device;
                }
            }
        }
        return null;
    }

    // Callback that handles all Bluetooth GATT events
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        // Called when connection state changes
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to Bluno!");
                // Once connected, start discovering services
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "Disconnected from Bluno. Attempting reconnect...");
                // Retry connection after delay if disconnected
                handler.postDelayed(() -> connectToBluno(), 3000);
            }
        }

        // Called when GATT services are discovered
        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered.");

                // Try to get the Bluno custom service using its UUID
                BluetoothGattService blunoService = gatt.getService(BLUNO_SERVICE_UUID);
                if (blunoService != null) {
                    // Try to get the characteristic used to communicate with Bluno
                    BluetoothGattCharacteristic blunoCharacteristic = blunoService.getCharacteristic(BLUNO_CHARACTERISTIC_UUID);
                    if (blunoCharacteristic != null) {
                        // Enable notifications on this characteristic
                        gatt.setCharacteristicNotification(blunoCharacteristic, true);

                        // Get the Client Characteristic Configuration Descriptor (CCCD)
                        BluetoothGattDescriptor descriptor = blunoCharacteristic.getDescriptor(CCCD_UUID);
                        if (descriptor != null) {
                            // Write value to CCCD to enable notifications
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        } else {
                            Log.e(TAG, "CCCD Descriptor not found!");
                        }
                    } else {
                        Log.e(TAG, "Bluno characteristic not found!");
                    }
                } else {
                    Log.e(TAG, "Bluno service not found!");
                }
            } else {
                Log.e(TAG, "Service discovery failed, status: " + status);
            }
        }

        // Buffer for partial BLE messages
        private final StringBuilder receivedDataBuffer = new StringBuilder();

        // Called when new data is received from Bluno via notification
        @SuppressLint("SetTextI18n")
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String receivedChunk = characteristic.getStringValue(0); // Get incoming chunk

            if (receivedChunk != null) {
                receivedDataBuffer.append(receivedChunk); // Append data to buffer

                Log.d(TAG, "Received Bluno Data Chunk: " + receivedChunk); // Debugging

                // Check if we have a complete message (based on expected message length or format)
                if (receivedDataBuffer.length() > 30 || receivedChunk.contains(":")) {
                    final String completeMessage = receivedDataBuffer.toString().trim(); // Get full message
                    receivedDataBuffer.setLength(0); // Clear buffer for next message

                    // Perform calculations using retrieved weight
                    double bacValue = extractBACValue(completeMessage);
                    double adjustedBAC = adjustBAC(bacValue, userWeight, userHeight);

                    Log.d("Scanning Activity", "Adjusted BAC: "+adjustedBAC);
                    if(count < 20){
                        bac_readings += bacValue;
                        readings.add(bacValue);
                        count++;
                    }

                    @SuppressLint("DefaultLocale") String finalMessage = completeMessage + " | Adjusted BAC: " + String.format("%.3f", adjustedBAC);

                    // Update UI on main thread
                    handler.post(() -> {
                        bacEntries.add(new BACEntry(bacValue, Timestamp.now()));
                        progressBar.setProgress(bacEntries.size()*5);
                        loadingTextView.setText("Loading latest BAC reading"+loading[count%3]);
                        adapter.notifyDataSetChanged();
                    });


                    Log.d(TAG, "Full BAC Result: " + completeMessage); // Log full message
                }

                // Once 20 readings are collected and if in LATEST_BAC mode
                if(count == 20 && MODE_LATEST_BAC){
                    // Use the highest of the 20 readings
                    bac_readings  = readings.stream().max(Double::compare).get();

                    // Create entries for storage
                    BACEntry bacEntry = new BACEntry(bac_readings, Timestamp.now());
                    Alert alert = new Alert(bac_readings, Timestamp.now());

                    // Save BAC entry and alert
                    storeBAC(bacEntry);
                    storeAlert(alert);

                    count++; // Prevents re-triggering this block

                    // Format for Intent
                    String bac_reading = String.format(Locale.US,"%.2f", bac_readings);

                    // Launch MainActivity with result and dashboard intent
                    Intent intent = new Intent(ScanningActivity.this, MainActivity.class);
                    intent.putExtra("latest_bac_entry", bac_reading);
                    intent.putExtra("toDashboard", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish(); // Finish scanning activity
                }
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] rawData = characteristic.getValue();

                if (rawData != null && rawData.length > 0) {
                    String receivedChunk = new String(rawData).trim();
                    receivedDataBuffer.append(receivedChunk);
                    Log.d(TAG, "Received Bluno Data Chunk: " + receivedChunk);

                    if (receivedChunk.contains("\n")) {
                        String completeMessage = receivedDataBuffer.toString();
                        receivedDataBuffer.setLength(0);
                        handler.post(() -> {
                            bacList.clear();
                            bacList.add(completeMessage.trim());
                            bacListAdapter.notifyDataSetChanged();
                        });

                        Log.d(TAG, "Full BAC Result: " + completeMessage);
                    }
                } else {
                    Log.e(TAG, "Received empty characteristic data!");
                }
            } else {
                Log.e(TAG, "Failed to read characteristic, status: " + status);
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void disconnectGatt() {
        if (mBluetoothGatt != null) {
            Log.d(TAG, "Disconnecting from Bluno...");
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    private void fetchUserData() {
        db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long heightValue = documentSnapshot.getLong("height");
                        Long weightValue = documentSnapshot.getLong("weight");

                        if (heightValue != null) {
                            userHeight = heightValue.intValue();
                        }
                        if (weightValue != null) {
                            userWeight = weightValue.intValue();
                        }

                        Log.d(TAG, "User Data Retrieved: Height=" + userHeight + ", Weight=" + userWeight);
                    } else {
                        Log.d(TAG, "User document does not exist. Using defaults.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching user data. Using defaults.", e));
    }


    private double adjustBAC(double bac, int weight, int height) {
        double bodyWaterConstant = 0.58; // Avg for males; 0.49 for females
        double metabolismRate = 0.017; // Per hour
        double bmiFactor = (height / 100.0) / Math.sqrt(weight); // Simplified BMI effect

        return (bac * (70.0 / weight)) * bodyWaterConstant * bmiFactor - metabolismRate;

        // TODO: 2025-04-08 Test before and after
    }


    private double extractBACValue(String message) {
        try {
            String[] parts = message.split("\\s+");
            double BAC_value_mean = 0;
            if (parts.length > 1) {
                for (String part : parts) {
                    BAC_value_mean += Double.parseDouble(part.trim());
                }
                return BAC_value_mean/parts.length;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing BAC value", e);
        }
        return 0.0;
    }

    private void saveLatestBAC(String bacValue) {
        SharedPreferences prefs = getSharedPreferences("DrinkWisePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("latestBAC", bacValue);
        editor.apply();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("latestBAC", bacValue);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public void storeBAC(BACEntry bacentry){
        db = FirebaseFirestore.getInstance();

        Map<String,Object> bac = new HashMap<>();
        bac.put("bacValue", bacentry.getBac());
        bac.put("Status", bacentry.getStatus());
        bac.put("Date", bacentry.getDate());
        bac.put("Time", bacentry.getTime());
        bac.put("Timestamp", Timestamp.now());

       DocumentReference documentReference = db.collection("users")
                .document(userId)
                .collection("BacEntry")
                .document(bacentry.getDate() + " " + bacentry.getTime());

       documentReference.get().addOnSuccessListener(documentSnapshot -> {
           if(documentSnapshot.exists()){
               documentReference.update(bac);
               Log.d("Firestore", "Bac entry updated successfully");
           }else{
               documentReference.set(bac);
               Log.d("Firestore", "Bac entry updated successfully");
           }
       }).addOnFailureListener(e -> Log.e("Firestore", "Error: "+e));
    }

    //Stores an alert object in firestore database when called. Updates if field already exists or sets if not
    public void storeAlert(Alert alert){
        db = FirebaseFirestore.getInstance();

        Map<String,Object> alertMap = new HashMap<>();
        alertMap.put("bacValue", alert.getBac());
        alertMap.put("SafetyLevel", alert.getSafetyLevel());
        alertMap.put("Message", alert.getMessage());
        alertMap.put("EscalationLevel", alert.getEscalationLevel());
        alertMap.put("Resolved", alert.isResolved());
        alertMap.put("Timestamp", Timestamp.now());

        DocumentReference documentReference = db.collection("users")
                .document(userId)
                .collection("Alerts")
                .document(alert.getDate() + " " + alert.getTime());

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                documentReference.update(alertMap);
                Log.d("Firestore", "Alert entry updated successfully");
            }else{
                documentReference.set(alertMap);
                Log.d("Firestore", "Alert entry saved successfully");
            }


            Log.d("Alert",alert.getMessage());

        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error: "+e);
        });
    }





    private void startFirestoreSafetyListener() {
        db.collection("users")
                .document(userId)
                .collection("BacEntry")
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                //When Time is comment out the popup shows when there is a status changed
                //However it doesn't get the latest time within the date
                //When Time is not comment out the popup does not shows when there is a status changed

                .limit(1)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Firestore listener error: ", e);
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        Log.d(TAG, "Firestore listener triggered but no documents found.");
                        return;
                    }
                    Log.d(TAG, "Document: " + snapshots.getDocuments().get(0).getData());

                    DocumentSnapshot doc = snapshots.getDocuments().get(0);
                    Log.d(TAG, "Snapshot received: " + doc.getData()); // 👈 Print the full document
                    Double bac = doc.getDouble("bacValue");
                    String currentStatus = doc.getString("Status");
                    Log.d(TAG, "Extracted fields -> BAC: " + bac + ", Status: " + currentStatus);


                    if (bac == null || currentStatus == null) {
                        Log.w(TAG, "Missing required fields in Firestore document!");
                        Log.w(TAG, "bacValue: " + bac + ", Status: " + currentStatus );
                        return;
                    }


                    // Detect status change
                    if (lastStatus != null && !lastStatus.equals(currentStatus)) {
                        long timeInPreviousState = (System.currentTimeMillis() - lastStatusTime) / 1000;
                        String title = "Safety Level Changed";
                        String message = "Status changed from " + lastStatus + " to " + currentStatus +
                                " after " + timeInPreviousState + "s.";

                        // use date/time strings
                        Alert alert = new Alert(bac, Timestamp.now());
                        storeAlert(alert);
                    }

                    // Repeated Danger detection
                    if ("Danger".equals(currentStatus)) {
                        dangerCount++;
                        if (dangerCount >= 3) {
                            String title = "⚠️ Repeated Danger Status";
                            String message = "You've had 3 or more consecutive 'Danger' readings.";

                            Alert alert = new Alert(bac, Timestamp.now());
                            storeAlert(alert);
                        }
                    } else {
                        dangerCount = 0;
                    }

                    lastStatus = currentStatus;
                    lastStatusTime = System.currentTimeMillis();
                });
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectGatt();
    }
}
