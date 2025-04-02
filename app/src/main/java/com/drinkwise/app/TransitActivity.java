package com.drinkwise.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class TransitActivity extends AppCompatActivity {

    private static final String TAG = "TransitActivity";

    //UI Components
    Button walk, transit, ride;
    //Home Address related variables
    private String address_line, city, province, postal_code, country;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit);

        fetchHomeAddress(((address_line1, city1, province1, postal_code1, country1) -> {
            Log.d(TAG, "Address line: "+address_line+" City: "+city+" Province: "+province+" Postal Code"+postal_code+" Country: "+country);
        }));



    }

    public void fetchHomeAddress(HomeAddressCallback callback){
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
                .document("Home_Address")
                .addSnapshotListener((value, error) -> {
                    if(error != null){
                        Log.d(TAG, "Error fetching data" + error);
                    }

                    if(value != null && value.exists()){
                        address_line = value.getString("Address_line");
                        city = value.getString("City");
                        province = value.getString("Province");
                        postal_code = value.getString("Postal_code");
                        country = value.getString("Country");

                        callback.onCallback(address_line, city, province, postal_code, country);

                    }
                    else {
                        address_line = "";
                        city = "";
                        province = "";
                        postal_code = "";
                        country = "";

                        callback.onCallback(address_line, city, province, postal_code, country);
                    }
                });
    }

    public void setupUI(){

        walk = findViewById(R.id.walk);
        transit = findViewById(R.id.transit);
        ride = findViewById(R.id.ride);
    }

    public interface HomeAddressCallback{
        void onCallback(String address_line, String city, String province, String postal_code, String country);
    }
}