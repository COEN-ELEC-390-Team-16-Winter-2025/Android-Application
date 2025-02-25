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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScanningActivity extends AppCompatActivity {

    private static final String TAG = "ScanningActivity";
    private static final String BLUNO_NAME = "Bluno";  // Change if needed
    private static final UUID BLUNO_SERVICE_UUID = UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");
    private static final UUID BLUNO_CHARACTERISTIC_UUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    private static final UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter bluetoothAdapter;
    private Handler handler;
    private ListView bacListView;
    private ArrayAdapter<String> bacListAdapter;
    private final List<String> bacList = new ArrayList<>();
    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice mBluetoothDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        bacListView = findViewById(R.id.bacListView);
        handler = new Handler(Looper.getMainLooper());

        // Set up the ListView adapter
        bacListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bacList);
        bacListView.setAdapter(bacListAdapter);

        // Initialize Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported");
            return;
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

                    // Update UI on main thread
                    handler.post(() -> {
                        bacList.add(completeMessage.trim()); // Append new reading instead of replacing
                        bacListAdapter.notifyDataSetChanged();
                    });


                    Log.d(TAG, "Full BAC Result: " + completeMessage); // Log full message
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectGatt();
    }
}
