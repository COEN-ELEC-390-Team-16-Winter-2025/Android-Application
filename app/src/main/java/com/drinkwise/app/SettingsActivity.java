package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private String TAG = "Settings Activity";
    //User's information variables
    private String userName;
    private long userHeight;
    private long userWeight;
    private boolean isMetric;
    private String birthday;

    //Emergency contact's information variables
    private String contact_name;
    private String contact_phone_no;
    private String contact_email;
    private String contact_relationship;

    //Change password related variables
    String currentPass, newPass, confirmPass;

    //Preferences related variables
    private boolean notifications, alerts, reminders, quickHelp;

    //UI Components
    protected TextView username_textview,  edit_profile_information, edit_physical_information, add_emergency_contact, edit_password;
    protected ImageView profile_picture, emergency_contact_profile_picture;
    private ActivityResultLauncher<Intent> launchGallery;
    protected EditText name_edit_text, height_edit_text, weight_edit_text, birthday_edit_text,
    emergency_contact_name, emergency_contact_phone_no, emergency_contact_email, emergency_contact_relationship,
    current_password, new_password, confirm_password;
    LinearLayout emergency_contact_layout;

    protected Button save_emergency_contact, save_profile_information, save_physical_information, save_password;
    protected RecyclerView settings_recycler_view;
    protected SettingsAdapter settingsAdapter;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    protected Switch notifications_switch, alerts_switch, reminders_switch, quick_help_switch;

    //Database related variables
    private FirebaseFirestore db;
    private String userId;
    private FirebaseUser user;
    private ArrayList<EmergencyContact> emergency_contacts = new ArrayList<>();

    @SuppressLint({"SetTextI18n", "IntentReset"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.custom_actionbar_title);
            TextView title = actionBar.getCustomView().findViewById(R.id.action_bar_title);
            title.setText("Settings");

            // Hide the Info and Profile icons
            ImageButton infoButton = actionBar.getCustomView().findViewById(R.id.info_button);
            ImageButton profileButton = actionBar.getCustomView().findViewById(R.id.profile_icon);

            // Set the custom back arrow on the right side without disturbing the title's position
            actionBar.setDisplayHomeAsUpEnabled(true);  // Enable the back button
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_settings);  // Set your custom dark brown arrow

            infoButton.setVisibility(View.GONE);
            profileButton.setVisibility(View.GONE);
        }

        launchGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->{
            if(result.getResultCode() == RESULT_OK && result.getData() != null){
                Uri uri = result.getData().getData();
                profile_picture.setImageURI(uri);
            }
        });

        setupUI(); //Initializes UI components

        //Fetches users information from firestore, edit the corresponding textview and edit texts
        fetchUserInformation((userName, userHeight, userWeight, isMetric, birthday) -> {

            username_textview.setText(userName);
            name_edit_text.setText(userName);

            if(!isMetric){
                height_edit_text.setText(userHeight + " ft");
                weight_edit_text.setText(userWeight + " lb");
            }
            else{
                height_edit_text.setText(userHeight + " cm");
                weight_edit_text.setText(userWeight + " kg");
            }

            birthday_edit_text.setText(birthday);

        });

        //Fetches the users preferences and edit the switches accordingly
        fetchPreferences((notifications, alerts, reminders, quickHelp) -> {
            notifications_switch.setChecked(notifications);
            alerts_switch.setChecked(alerts);
            reminders_switch.setChecked(reminders);
            quick_help_switch.setChecked(quickHelp);
        });

        setupRecyclerView();

        profile_picture.setOnClickListener(v -> {
            @SuppressLint("IntentReset") Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            launchGallery.launch(intent);
        });

        //Toggles visibility of save profile information button when clicking on edit
        edit_profile_information.setOnClickListener(v -> {
            if(save_profile_information.getVisibility() == TextView.GONE){
                save_profile_information.setVisibility(TextView.VISIBLE);
                name_edit_text.setText("");
                birthday_edit_text.setText("");
                name_edit_text.setEnabled(true);
                birthday_edit_text.setEnabled(true);

            }
            else{
                save_profile_information.setVisibility(TextView.GONE);
                name_edit_text.setText(userName);
                birthday_edit_text.setText(birthday);
                name_edit_text.setEnabled(false);
                birthday_edit_text.setEnabled(false);
            }
        });

        //Toggles visibility of save physical information button when clicking on edit
        edit_physical_information.setOnClickListener(v -> {
            if(save_physical_information.getVisibility() == TextView.GONE){
                save_physical_information.setVisibility(TextView.VISIBLE);
                height_edit_text.setText("");
                weight_edit_text.setText("");
                height_edit_text.setEnabled(true);
                weight_edit_text.setEnabled(true);
            }
            else{
                save_physical_information.setVisibility(TextView.GONE);

                if(!isMetric){
                    height_edit_text.setText(userHeight+ " ft");
                    weight_edit_text.setText(userWeight+ " lb");
                }
                else{
                    height_edit_text.setText(userHeight+ " cm");
                    weight_edit_text.setText(userWeight+ " kg");
                }

                height_edit_text.setEnabled(false);
                weight_edit_text.setEnabled(false);
            }
        });

        edit_password.setOnClickListener(v -> {
            if(save_password.getVisibility() == TextView.GONE){
                save_password.setVisibility(TextView.VISIBLE);
                current_password.setEnabled(true);
                new_password.setEnabled(true);
                confirm_password.setEnabled(true);
            }
            else{
                save_password.setVisibility(TextView.GONE);
                current_password.setEnabled(false);
                new_password.setEnabled(false);
                confirm_password.setEnabled(false);
                current_password.setText("");
                new_password.setText("");
                confirm_password.setText("");
            }
        });

        save_password.setOnClickListener(v -> {
            currentPass = current_password.getText().toString();
            newPass = new_password.getText().toString();
            confirmPass = confirm_password.getText().toString();

            if(currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()){
                Toast.makeText(this, "Please, fill out all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!newPass.equals(confirmPass)){
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            }
            else{
                reauthenticateUserAndChangePass(currentPass, newPass);
            }
        });

        //Toggles visibility of add_emergency_contact_layout
        add_emergency_contact.setOnClickListener(v -> {
            if(emergency_contact_layout.getVisibility() == TextView.GONE){
                emergency_contact_layout.setVisibility(TextView.VISIBLE);
            }
            else {
                emergency_contact_layout.setVisibility(TextView.GONE);
            }
        });

        save_profile_information.setOnClickListener(v -> {

            userName = name_edit_text.getText().toString();
            birthday = birthday_edit_text.getText().toString();

            store_user_information(userName, birthday, userHeight, userWeight, isMetric);

            name_edit_text.setEnabled(false);
            name_edit_text.setText(userName);
            birthday_edit_text.setEnabled(false);
            birthday_edit_text.setText(birthday);
            username_textview.setText(userName);

            save_profile_information.setVisibility(TextView.GONE);
        });

        save_physical_information.setOnClickListener(v -> {
            userWeight = Long.parseLong(weight_edit_text.getText().toString());
            userHeight = Long.parseLong(height_edit_text.getText().toString());

            store_user_information(userName, birthday, userHeight, userWeight, isMetric);

            height_edit_text.setEnabled(false);
            weight_edit_text.setEnabled(false);

            if(!isMetric){
                height_edit_text.setText(userHeight+ " ft");
                weight_edit_text.setText(userWeight+ " lb");
            }
            else{
                height_edit_text.setText(userHeight+ " cm");
                weight_edit_text.setText(userWeight+ " kg");
            }

            save_physical_information.setVisibility(TextView.GONE);
        });

        save_emergency_contact.setOnClickListener(v -> {
            contact_name = emergency_contact_name.getText().toString();
            contact_phone_no = emergency_contact_phone_no.getText().toString();
            contact_email = emergency_contact_email.getText().toString();
            contact_relationship = emergency_contact_relationship.getText().toString();

            if(contact_name.isEmpty() || contact_phone_no.isEmpty() || contact_email.isEmpty() || contact_relationship.isEmpty()){
                Toast.makeText(this, "Please, fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            store_emergency_contact(contact_name, contact_phone_no, contact_email, contact_relationship);

            emergency_contact_name.setText(" ");
            emergency_contact_phone_no.setText(" ");
            emergency_contact_email.setText(" ");
            emergency_contact_relationship.setText(" ");

            emergency_contact_layout.setVisibility(TextView.GONE);
        });

        // Load and handle the reminders toggle
//        loadReminderPreference();
//        reminders_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                saveReminderPreference(isChecked);
//                if (!isChecked) {
//                    // Stop reminders if disabled
//                    Log.d("ReminderTesting", "Reminders have been disabled.");
//                    ReminderManager.getInstance(SettingsActivity.this).stopReminders();
//                } else {
//                    Log.d("ReminderTesting", "Reminders have been enabled.");
//                }
//            }
//        });
      
        //Updates firestore preferences when notification switch is changed
        notifications_switch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if(isChecked){
                notifications = true;
                storePreferences(notifications, alerts, reminders, quickHelp);
            }
            else{
                notifications = false;
                storePreferences(notifications, alerts, reminders, quickHelp);
            }
        }));

        //Updates firestore preferences when alerts switch is changed
        alerts_switch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if(isChecked){
                alerts = true;
                storePreferences(notifications, alerts, reminders, quickHelp);
            }
            else{
                alerts = false;
                storePreferences(notifications, alerts, reminders, quickHelp);
            }
        }));

        //Updates firestore preferences when reminders switch is changed
        reminders_switch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if(isChecked){
                reminders = true;
                storePreferences(notifications, alerts, reminders, quickHelp);
            }
            else{
                reminders = false;
                storePreferences(notifications, alerts, reminders, quickHelp);
            }
        }));

        quick_help_switch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if(isChecked){
                quickHelp = true;
                storePreferences(notifications, alerts, reminders, quickHelp);
            }
            else{
                quickHelp = false;
                storePreferences(notifications, alerts, reminders, quickHelp);
            }
        }));

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // If the home (back) button is clicked, finish the activity
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);  // Pass the event to the superclass for default behavior
    }

    //This function fetches all the user's information from firestore and stores them in their respective variables
    public void fetchUserInformation(onDataFetched callback){
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Log.e(TAG, "No logged-in user found.");
        }

        db.collection("users")
                .document(userId)
                .collection("profile")
                .document("stats")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userName = documentSnapshot.getString("name");
                        userName = documentSnapshot.getString("name");

                        Long heightVal = documentSnapshot.getLong("height");
                        userHeight = (heightVal != null) ? heightVal : 0;  // Default to 0 if null

                        Long weightVal = documentSnapshot.getLong("weight");
                        userWeight = (weightVal != null) ? weightVal : 0;  // Default to 0 if null

                        Boolean metricVal = documentSnapshot.getBoolean("isMetric");
                        isMetric = (metricVal != null) ? metricVal : false;  // Default to false if null

                        birthday = documentSnapshot.getString("birthday");
                        birthday = documentSnapshot.getString("birthday");

                        Log.d(TAG, "Username: "+userName + " userHeight: "+userHeight+" userWeight: "+userWeight+" isMetric: "+ isMetric+ " birthday: "+birthday);

                        //ensures data is completely fetched before proceeding
                        callback.Fetched(userName, userHeight, userWeight, isMetric, birthday);
                    } else {
                        Log.d(TAG, "No data found for this user");
                    }

                })
                .addOnFailureListener(e -> Log.d(TAG, "Error fetching data"));
    }


    public void fetchEmergencyContacts(EmergencyContactsCallback callback){

        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Log.e(TAG, "No logged-in user found.");
        }

        db.collection("users")
                .document(userId)
                .collection("profile")
                .document("Contacts")
                .collection("Emergency_Contacts")
                .addSnapshotListener((value, error) -> {

                    if(error != null){
                        Log.d("Firestore", "Error fetching emergency contacts" + error);
                    }

                    if(value != null && !value.isEmpty()){

                        emergency_contacts.clear();

                        for(DocumentSnapshot doc : value.getDocuments()){
                            String name = doc.getString("Name");
                            String phone_no = doc.getString("Phone_no");
                            String email = doc.getString("Email");
                            String relationship = doc.getString("Relationship");

                            emergency_contacts.add(new EmergencyContact(name, phone_no, email, relationship));

                            Log.d(TAG, "Emergency Contact: Name: "+name+" Phone no: "+phone_no+" Email: "+email+" Relationship: "+relationship);
                            callback.onCallback(emergency_contacts);
                        }
                    }
                    else{
                        Log.d(TAG, "No Emergency Contacts found");
                        callback.onCallback(emergency_contacts);
                    }
                });
    }

    public void fetchPreferences(PreferencesCallback callback){

        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Log.e(TAG, "No logged-in user found.");
        }

        db.collection("users")
                .document(userId)
                .collection("profile")
                .document("Preferences")
                .addSnapshotListener(((value, error) -> {
                    if(error != null){
                        Log.d(TAG, "Error fetching preferences");
                    }

                    if(value != null && value.exists()){
                        notifications = Boolean.TRUE.equals(value.getBoolean("Notifications"));
                        alerts = Boolean.TRUE.equals(value.getBoolean("Alerts"));
                        reminders = Boolean.TRUE.equals(value.getBoolean("Reminders"));
                        quickHelp = Boolean.TRUE.equals(value.getBoolean("Quick_help"));

                        Log.d(TAG, "Notifications: "+notifications + " Alerts: "+alerts+" Reminders: "+reminders+" Quick Help: "+quickHelp);

                        callback.onCallback(notifications, alerts, reminders, quickHelp);
                    }
                    else{
                        notifications = false;
                        alerts = false;
                        reminders = false;
                        quickHelp = false;

                        callback.onCallback(notifications, alerts, reminders, quickHelp);
                    }
                }));
    }

    public void reauthenticateUserAndChangePass(String currentP, String newP){

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        String email = Objects.requireNonNull(user).getEmail();

        AuthCredential credentials = EmailAuthProvider.getCredential(Objects.requireNonNull(email), currentP);

        user.reauthenticate(credentials)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d(TAG, "User authenticated successfully");
                        changePassword(newP);
                    }
                    if(!task.isSuccessful()){
                        Log.d(TAG, "Incorrect Password");
                        Toast.makeText(this, "Your current password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void changePassword(String newP){
        user.updatePassword(newP)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.d(TAG, "Error changing password");
                    }
                });


    }

    //This function stores an emergency contact specified in firestore database
    public void store_emergency_contact(String name, String phone_no, String email, String relationship){

        Map<String,Object> emergency_contact = new HashMap<>();
        emergency_contact.put("Name", name);
        emergency_contact.put("Phone_no", phone_no);
        emergency_contact.put("Email", email);
        emergency_contact.put("Relationship", relationship);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Log.e(TAG, "No logged-in user found.");
        }

        db.collection("users")
                .document(userId)
                .collection("profile")
                .document("Contacts")
                .collection("Emergency_Contacts")
                .add(emergency_contact)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Emergency contact saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Log.d("Firestore", "Failed to add an emergency contact" + e));
    }

    public void store_user_information(String name, String birthday, long height, long weight, boolean isMetric){

        Map<String,Object> profile_information = new HashMap<>();

        profile_information.put("name", name);
        profile_information.put("birthday", birthday);
        profile_information.put("height", height);
        profile_information.put("weight", weight);
        profile_information.put("isMetric", isMetric);

        db.collection("users")
                .document(userId)
                .collection("profile")
                .document("stats")
                .set(profile_information)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Log.d("Firestore", "Failed to store data "+ e));
    }

    public void storePreferences(boolean notifications, boolean alerts, boolean reminders, boolean quickHelp){

        Map<String, Object> preferences = new HashMap<>();

        preferences.put("Notifications", notifications);
        preferences.put("Alerts", alerts);
        preferences.put("Reminders", reminders);
        preferences.put("Quick_help", quickHelp);

        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Log.e(TAG, "No logged-in user found.");
        }

        db.collection("users")
                .document(userId)
                .collection("profile")
                .document("Preferences")
                .set(preferences)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Preferences saved."))
                .addOnFailureListener(e ->
                        Log.d("Firestore", "Failed to store data "+ e));
    }

    //This function initializes all ui components
    public void setupUI(){
        //Username and profile picture UI components
        username_textview = findViewById(R.id.account_name);
        profile_picture = findViewById(R.id.profile_picture);

        //Profile information UI components
        name_edit_text = findViewById(R.id.name_edit_text);
        birthday_edit_text = findViewById(R.id.birthday_edit_text);
        edit_profile_information = findViewById(R.id.edit_profile_information);
        save_profile_information = findViewById(R.id.save_profile_information);

        //Physical information UI components
        height_edit_text = findViewById(R.id.height_edit_text);
        weight_edit_text = findViewById(R.id.weight_edit_text);
        edit_physical_information = findViewById(R.id.edit_physical_information);
        save_physical_information = findViewById(R.id.save_physical_information);

        //Emergency contacts UI components
        add_emergency_contact = findViewById(R.id.add_emergency_contact);
        emergency_contact_layout = findViewById(R.id.emergency_contact_layout);
        emergency_contact_profile_picture = findViewById(R.id.emergency_contact_profile_picture);
        emergency_contact_name = findViewById(R.id.emergency_contact_name);
        emergency_contact_phone_no = findViewById(R.id.emergency_contact_phone_no);
        emergency_contact_email = findViewById(R.id.emergency_contact_email);
        emergency_contact_relationship = findViewById(R.id.emergency_contact_relationship);
        save_emergency_contact = findViewById(R.id.save_emergency_contact);
        settings_recycler_view = findViewById(R.id.emergency_contact_recycler_view);

        //Change Password related UI components
        edit_password = findViewById(R.id.edit_password);
        current_password = findViewById(R.id.current_password);
        new_password = findViewById(R.id.new_password);
        confirm_password = findViewById(R.id.confirm_password);
        save_password = findViewById(R.id.save_password);


        //Preferences UI components
        notifications_switch = findViewById(R.id.notifications_switch);
        alerts_switch = findViewById(R.id.alerts_switch);
        reminders_switch = findViewById(R.id.reminders_switch);
        quick_help_switch = findViewById(R.id.quick_help_switch);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setupRecyclerView() {
        fetchEmergencyContacts(emergency_contacts -> {
            Log.d(TAG, "Data Fetched Successfully");

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            settingsAdapter = new SettingsAdapter(emergency_contacts);

            settings_recycler_view.setLayoutManager(linearLayoutManager);
            settings_recycler_view.setAdapter(settingsAdapter);
            settingsAdapter.notifyDataSetChanged();

        });
    }

    private void loadReminderPreference() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean remindersEnabled = prefs.getBoolean("remindersEnabled", true);
        reminders_switch.setChecked(remindersEnabled);
        Log.d("ReminderTesting", "Reminders have been enabled.");
    }

    private void saveReminderPreference(boolean enabled) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("remindersEnabled", enabled).apply();
        Toast.makeText(this, "Reminders are now " + (enabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
    }

    public interface onDataFetched{
        void Fetched(String userName, long userHeight, long userWeight, boolean isMetric, String birthday);
    }

    //Interface used for the callback for fetching of emergency contacts. Fetching is asynchronous so need to wait for the data to be fetched before proceeding
    public interface EmergencyContactsCallback {
        void onCallback(ArrayList<EmergencyContact> contacts);
    }

    public interface PreferencesCallback {
        void onCallback(boolean notifications, boolean alerts, boolean reminders, boolean quickHelp);
    }

    //Tried to use this method to hide info button from settings page
    private void setupCustomActionBar(boolean showInfoButton, String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            LayoutInflater inflater = LayoutInflater.from(this);
            @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.custom_actionbar_title, null);

            TextView actionBarTitle = customView.findViewById(R.id.action_bar_title);
            actionBarTitle.setText(title);

            ImageButton infoButton = customView.findViewById(R.id.info_button);
            if (showInfoButton) {
                infoButton.setVisibility(View.VISIBLE);
                infoButton.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, InfoActivity.class)));
            } else {
                infoButton.setVisibility(View.GONE);
            }

            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL
            );
            actionBar.setCustomView(customView, layoutParams);
        }
    }
}