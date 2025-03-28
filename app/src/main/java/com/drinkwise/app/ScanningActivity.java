package com.drinkwise.app;

import android.annotation.SuppressLint;
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
import androidx.appcompat.widget.Toolbar;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;



public class ScanningActivity extends AppCompatActivity {

    private static final String TAG = "ScanningActivity";
    private static final String BLUNO_NAME = "Bluno";  // Change if needed
    private static final UUID BLUNO_SERVICE_UUID = UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");
    private static final UUID BLUNO_CHARACTERISTIC_UUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    private static final UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter bluetoothAdapter;
    private Handler handler;
    private ListView bacListView;
    private TextView loadingTextView;
    private ProgressBar progressBar;
    private TextView instructionTextView;

    private boolean MODE_LATEST_BAC;
    String[] loading = {".", "..", "..."};

    private double bac_readings = 0.0;
    private ArrayList<Double> readings = new ArrayList<>();
    private int count = 0;
    private static int entry_count = 0;
    private ArrayAdapter<String> bacListAdapter;
    private ScanningAdapter adapter;
    private final List<String> bacList = new ArrayList<>();
    private ArrayList<BACEntry> bacEntries = new ArrayList<BACEntry>();
    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice mBluetoothDevice;

    private FirebaseFirestore db;
    private int userHeight = 170;  // Default height (cm)
    private int userWeight = 70;   // Default weight (kg)
    private String userId = "USER_ID_HERE"; // Replace with actual user ID

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
        } else {
            Log.e(TAG, "No logged-in user found.");
        }


        bacListView = findViewById(R.id.bacListView);
        loadingTextView = findViewById(R.id.loadingTextView);
        progressBar = findViewById(R.id.progressBar);
        instructionTextView = findViewById(R.id.instructionTextView);

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
            mBluetoothDevice = device;
            mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallback);
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

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to Bluno!");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "Disconnected from Bluno. Attempting reconnect...");
                handler.postDelayed(() -> connectToBluno(), 3000);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered.");
                BluetoothGattService blunoService = gatt.getService(BLUNO_SERVICE_UUID);
                if (blunoService != null) {
                    BluetoothGattCharacteristic blunoCharacteristic = blunoService.getCharacteristic(BLUNO_CHARACTERISTIC_UUID);
                    if (blunoCharacteristic != null) {
                        gatt.setCharacteristicNotification(blunoCharacteristic, true);
                        BluetoothGattDescriptor descriptor = blunoCharacteristic.getDescriptor(CCCD_UUID);
                        if (descriptor != null) {
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

        private final StringBuilder receivedDataBuffer = new StringBuilder();

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

                    String finalMessage = completeMessage + " | Adjusted BAC: " + String.format("%.3f", adjustedBAC);

                    // Update UI on main thread
                    handler.post(() -> {
                        bacEntries.add(new BACEntry(bacValue, Timestamp.now()));
                        progressBar.setProgress(bacEntries.size()*5);
                        loadingTextView.setText("Loading latest BAC reading"+loading[count%3]);
                        adapter.notifyDataSetChanged();
                    });


                    Log.d(TAG, "Full BAC Result: " + completeMessage); // Log full message
                }
                if(count == 20 && MODE_LATEST_BAC){
                    bac_readings  = readings.stream().max(Double::compare).get();
                    BACEntry bacEntry = new BACEntry(bac_readings, Timestamp.now());
                    Alert alert = new Alert(bac_readings, Timestamp.now());
                    storeBAC(bacEntry);
                    storeAlert(alert);
                    count++;
                    String bac_reading = String.format(Locale.US,"%.2f", bac_readings);
                    Intent intent = new Intent(ScanningActivity.this, MainActivity.class);
                    intent.putExtra("latest_bac_entry", bac_reading);
                    intent.putExtra("toDashboard", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
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
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data. Using defaults.", e);
                });
    }


    private double adjustBAC(double bac, int weight, int height) {
        double bodyWaterConstant = 0.58; // Avg for males; 0.49 for females
        double metabolismRate = 0.017; // Per hour
        double bmiFactor = (height / 100.0) / Math.sqrt(weight); // Simplified BMI effect

        return (bac * (70.0 / weight)) * bodyWaterConstant * bmiFactor - metabolismRate;
    }


    private double extractBACValue(String message) {
        try {
            String[] parts = message.split("\\s+");
            double BAC_value_mean = 0;
            if (parts.length > 1) {
                for(int i=0; i<parts.length; i++){
                    BAC_value_mean += Double.parseDouble(parts[i].trim());
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
       }).addOnFailureListener(e -> {
           Log.e("Firestore", "Error: "+e);
       });
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
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error: "+e);
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectGatt();
    }
}
