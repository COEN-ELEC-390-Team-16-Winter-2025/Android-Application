package com.drinkwise.app;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;

public class TransitActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "TransitActivity";
    private static final String API_KEY = "AIzaSyDNqAmUya8Zkt3YO9kts3a9D-dAgY67sAI";

    //UI Components
    protected Button walk, transit, ride;
    protected MapView mapView;
    //Home Address related variables
    private String address_line, city, province, postal_code, country;

    //Google Map Related Variables
    private GoogleMap gMap;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit);

        setupUI();

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

        walk.setOnClickListener(v -> {
            mapView.setVisibility(MapView.VISIBLE);
            AddressToGeo(address_line, city, province, postal_code, country);

        });
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
    }

    public void AddressToGeo(String address_line, String city, String province, String postal_code, String country){
        String address = address_line+", "+city+", "+province+", "+postal_code+", "+country;

        String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                + Uri.encode(address) + "&key=" + API_KEY;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            Log.d("Response", response.toString());
            try{
                //TODO: remove later
                String status = response.getString("status");
                if (!status.equals("OK")) {
                    Log.e(TAG, "Geocoding API error: " + status);
                    return;
                }

                JSONArray result = response.getJSONArray("results");

                //TODO: REMOVE LATER
                if (result.length() == 0) {
                    Log.e(TAG, "No results found for the given address");
                    return;
                }

                double homeLat = result.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                double homeLong = result.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                Log.d(TAG, "Home Latitude: "+homeLat+" Home Longitude: "+homeLong);
            } catch (Exception e) {
                Log.d(TAG, "Error fetching the latitude and longitude" + e);
            }
        },
                error -> {

                    Log.e(TAG, "Error fetching results" + error);
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    public void setupUI(){

        walk = findViewById(R.id.walk);
        transit = findViewById(R.id.transit);
        ride = findViewById(R.id.ride);
        mapView = findViewById(R.id.Map);

        Bundle mapViewBundle = null;

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    public interface HomeAddressCallback{
        void onCallback(String address_line, String city, String province, String postal_code, String country);
    }
}