package com.drinkwise.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.drinkwise.app.ui.notifications.ReminderManager;

import java.util.HashMap;
import java.util.Map;

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

    //UI Components
    protected TextView username_textview,  edit_profile_information, edit_physical_information, add_emergency_contact;
    protected ImageView profile_picture, emergency_contact_profile_picture;
    private ActivityResultLauncher<Intent> launchGallery;
    protected EditText name_edit_text, height_edit_text, weight_edit_text, birthday_edit_text,
            emergency_contact_name, emergency_contact_phone_no, emergency_contact_email, emergency_contact_relationship;
    LinearLayout emergency_contact_layout;

    protected Button save_emergency_contact, save_profile_information, save_physical_information;


    // switch to enable/disable reminders
    private Switch switchReminders;

    //Database related variables
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        launchGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->{
            if(result.getResultCode() == RESULT_OK && result.getData() != null){
                Uri uri = result.getData().getData();
                profile_picture.setImageURI(uri);
            }
        });
        setupUI();
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

        profile_picture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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

            emergency_contact_name.setText("");
            emergency_contact_phone_no.setText("");
            emergency_contact_email.setText("");
            emergency_contact_relationship.setText("");

            emergency_contact_layout.setVisibility(TextView.GONE);
        });

        // Load and handle the reminders toggle
        loadReminderPreference();
        switchReminders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveReminderPreference(isChecked);
                if (!isChecked) {
                    // Stop reminders if disabled
                    Log.d("ReminderTesting", "Reminders have been disabled.");
                    ReminderManager.getInstance(SettingsActivity.this).stopReminders();
                } else {
                    Log.d("ReminderTesting", "Reminders have been enabled.");
                }
            }
        });
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
                        userHeight = documentSnapshot.getLong("height");
                        userWeight = documentSnapshot.getLong("weight");
                        isMetric = documentSnapshot.getBoolean("isMetric");
                        birthday = documentSnapshot.getString("birthday");

                        Log.d(TAG, "Username: "+userName + " userHeight: "+userHeight+" userWeight: "+userWeight+" isMetric: "+ isMetric+ " birthday: "+birthday);

                        //ensures data is completely fetched before proceeding
                        callback.Fetched(userName, userHeight, userWeight, isMetric, birthday);
                    } else {
                        Log.d(TAG, "No data found for this user");
                    }

                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error fetching data");
                });
    }

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
        switchReminders = findViewById(R.id.switch_enable_reminders);
    }

    private void loadReminderPreference() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean remindersEnabled = prefs.getBoolean("remindersEnabled", true);
        switchReminders.setChecked(remindersEnabled);
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
}