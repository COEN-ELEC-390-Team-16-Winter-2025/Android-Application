package com.drinkwise.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private String TAG = "Settings Activity";
    //User's information variables
    private String userName;
    private long userHeight;
    private long userWeight;
    private boolean isMetric;
    private String birthday;

    //UI Components
    protected TextView username_textview;
    protected ImageView profile_picture;
    private ActivityResultLauncher<Intent> launchGallery;
    protected EditText name_edit_text, height_edit_text, weight_edit_text, birthday_edit_text;

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
                    }
                    else{
                        Log.d(TAG, "No data found for this user");
                    }

                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error fetching data");
                });
    }

    //This function initializes all ui components
    public void setupUI(){
        username_textview = findViewById(R.id.account_name);
        profile_picture = findViewById(R.id.profile_picture);
        name_edit_text = findViewById(R.id.name_edit_text);
        height_edit_text = findViewById(R.id.height_edit_text);
        weight_edit_text = findViewById(R.id.weight_edit_text);
        birthday_edit_text = findViewById(R.id.birthday_edit_text);
    }


    //onDataFetched interface. Needed because fetching from firestore is asynchronous and we need to wait for the data to be fetched before proceeding
    public interface onDataFetched{
        void Fetched(String userName, long userHeight, long userWeight, boolean isMetric, String birthday);
    }
}
