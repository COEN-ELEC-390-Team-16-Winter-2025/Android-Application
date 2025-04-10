package com.drinkwise.app;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.core.View;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TransitActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "TransitActivity";

    //needed for access to google maps api
    private static final String API_KEY = "AIzaSyDNqAmUya8Zkt3YO9kts3a9D-dAgY67sAI";
    private static final String uberClientID = "3ilDPXY3GKbN3JLLzEIjUVt7h4L1z_ZX";

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

        Bundle mapViewBundle = null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        fetchHomeAddress(((address_line1, city1, province1, postal_code1, country1) -> {
            Log.d(TAG, "Address line: "+address_line+" City: "+city+" Province: "+province+" Postal Code"+postal_code+" Country: "+country);
        }));

        walk.setOnClickListener(v -> {
            walk.setVisibility(MapView.GONE);
            transit.setVisibility(MapView.GONE);
            ride.setVisibility(MapView.GONE);
            mapView.setVisibility(MapView.VISIBLE);
            AddressToGeo(address_line, city, province, postal_code, country, "walking");
        });

        transit.setOnClickListener(v -> {
            walk.setVisibility(MapView.GONE);
            transit.setVisibility(MapView.GONE);
            ride.setVisibility(MapView.GONE);
            mapView.setVisibility(MapView.VISIBLE);
            AddressToGeo(address_line, city, province, postal_code, country, "transit");
        });

        ride.setOnClickListener(v -> {
            walk.setVisibility(MapView.GONE);
            transit.setVisibility(MapView.GONE);
            ride.setVisibility(MapView.GONE);
            AddressToGeo(address_line, city, province, postal_code, country, "ride");
        });

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
                        Log.d(TAG, "Error fetcgit ing data" + error);
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



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
    }

    public void AddressToGeo(String address_line, String city, String province, String postal_code, String country, String mode){
        String address = address_line+", "+city+", "+province+", "+postal_code+", "+country;

        String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                + Uri.encode(address) + "&key=" + API_KEY;

        //Instantiates a requestQueue using volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Http request: GET
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try{

                JSONArray result = response.getJSONArray("results");

                //extracts the latitude and longitude from the result
                double homeLat = result.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                double homeLong = result.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                fetchCurrentLocation(homeLat, homeLong, mode);


                //error handling
                Log.d(TAG, "Home Latitude: "+homeLat+" Home Longitude: "+homeLong);
            } catch (Exception e) {
                Log.d(TAG, "Error fetching the latitude and longitude" + e);
            }
        },
                error -> {

                    Log.e(TAG, "Error fetching results" + error);
                }
        );

        //add jsonobjectrequest to request queue to get executed
        requestQueue.add(jsonObjectRequest);
    }
    public void fetchCurrentLocation(double destinationLat, double destinationLong, String mode){

        //fused location provider to get current location
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Need to get permission for location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request location permission if permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1001);
            return;
        }


        //gets last location
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    //fetches the latitude and longitude
                    double currentLat = location.getLatitude();
                    double currentLong = location.getLongitude();

                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat, currentLong), 17));


                    Log.d(TAG, "Current Latitude: "+currentLat+ " Current Longitude: "+currentLong);

                    switch(mode){
                        case "walking": fetchWalkableRoute(currentLat, currentLong, destinationLat, destinationLong);
                        break;
                        case "transit": fetchTransitRoute(currentLat, currentLong, destinationLat, destinationLong);
                        break;
                        case "ride": openUberApp(currentLat, currentLong, destinationLat, destinationLong);
                    }


                });
    }
    public void fetchWalkableRoute(double currentLat, double currentLong, double destinationLat, double destinationLong){
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+
                currentLat+","+currentLong+
                "&destination="+destinationLat+","+destinationLong+
                "&mode=walking"+"&key="+API_KEY;

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {

                        //fetches all the routes from the response
                        JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() == 0) {
                            Log.e("Directions", "No walking route found.");
                            return;
                        }


                        //takes the first route (the fastest one)
                        JSONObject route = routes.getJSONObject(0);


                        //extracts the legs (waypoints)
                        JSONArray legs = route.getJSONArray("legs");
                        //takes the first leg (contains the full route)
                        JSONObject leg = legs.getJSONObject(0);

                        String distance = leg.getJSONObject("distance").getString("text");
                        String duration = leg.getJSONObject("duration").getString("text");

                        Log.d(TAG, "Distance: "+distance+" Duration:"+duration);

                        //extracts the steps (turns of the route)
                        JSONArray steps = leg.getJSONArray("steps");

                        ArrayList<LatLng> path = new ArrayList<>();
                        for(int i = 0; i<steps.length(); i++){

                            //extracts the polyline from the steps and adds it to the path
                            String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                            path.addAll(PolyUtil.decode(polyline));

                        }

                        PolylineOptions polylineOptions = new PolylineOptions().addAll(path).color(Color.BLUE).width(10);
                        //adds markers at the start and end to the map
                        gMap.addMarker(new MarkerOptions().position(new LatLng(currentLat, currentLong)).title("Start"));
                        gMap.addMarker(new MarkerOptions().position(new LatLng(destinationLat, destinationLong)).title("Arrival"));
                        //adds polyline route to the map
                        gMap.addPolyline(polylineOptions);

                    } catch (Exception e) {
                        Log.e("Directions", "Error parsing route", e);
                    }
                },
                error -> Log.e("Directions", "Request failed: " + error));

        requestQueue.add(jsonObjectRequest);
    }

    public void fetchTransitRoute(double currentLat, double currentLong, double destinationLat, double destinationLong){
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+
                currentLat+","+currentLong+
                "&destination="+destinationLat+","+destinationLong+
                "&mode=transit"+
                "&transit_mode=bus|subway|tram"+
                "&transit_routing_preference=less_walking"+"&key="+API_KEY;

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() == 0) {
                            Log.e("Directions", "No walking route found.");
                            return;
                        }

                        JSONObject route = routes.getJSONObject(0);

                        JSONArray legs = route.getJSONArray("legs");
                        JSONObject leg = legs.getJSONObject(0);

                        String distance = leg.getJSONObject("distance").getString("text");
                        String duration = leg.getJSONObject("duration").getString("text");

                        Log.d(TAG, "Distance: "+distance+" Duration:"+duration);

                        JSONArray steps = leg.getJSONArray("steps");
                        ArrayList<LatLng> path = new ArrayList<>();
                        for(int i = 0; i<steps.length(); i++){

                            JSONObject step = steps.getJSONObject(i);
                            if(step.has("transit_details")){
                                JSONObject transitDetails = step.getJSONObject("transit_details");

                                String vehicleType = transitDetails.getJSONObject("line").getJSONObject("vehicle").getString("type");
                                String lineName = transitDetails.getJSONObject("line").getString("name");
                                String shortName = transitDetails.getJSONObject("line").optString("short_name", "N/A");
                                String departureStop = transitDetails.getJSONObject("departure_stop").getString("name");
                                String arrivalStop = transitDetails.getJSONObject("arrival_stop").getString("name");
                                int numberOfStops = transitDetails.getInt("num_stops");

                                Log.d(TAG, "Vehicle: " + vehicleType + " Line: " + lineName + " (" + shortName + ")");
                                Log.d(TAG, "From: " + departureStop + " To: " + arrivalStop);
                                Log.d(TAG, "Number of Stops: " + numberOfStops);

                                LatLng departure = new LatLng(transitDetails.getJSONObject("departure_stop").getJSONObject("location").getDouble("lat"),
                                        transitDetails.getJSONObject("departure_stop").getJSONObject("location").getDouble("lng"));


                                Marker marker = gMap.addMarker(new MarkerOptions()
                                        .position(departure)
                                        .title("Departure: " + departureStop)
                                        .snippet(vehicleType + " - " +shortName+ " - "+lineName+" - "+"Number of stops: " + numberOfStops)
                                );

                            }
                            String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                            path.addAll(PolyUtil.decode(polyline));

                        }

                        PolylineOptions polylineOptions = new PolylineOptions().addAll(path).color(Color.BLUE).width(10);
                        gMap.addMarker(new MarkerOptions().position(new LatLng(currentLat, currentLong)).title("Start"));
                        gMap.addMarker(new MarkerOptions().position(new LatLng(destinationLat, destinationLong)).title("Arrival"));
                        gMap.addPolyline(polylineOptions);

                    } catch (Exception e) {
                        Log.e("Directions", "Error fetching route", e);
                    }
                },
                error -> Log.e("Directions", "Error fetching route" + error));

        requestQueue.add(jsonObjectRequest);
    }

    public void openUberApp(double currentLat, double currentLong, double destinationLat, double destinationLong){

        String uberUrl = "https://m.uber.com/ul/?action=setPickup" +
                "&client_id=" + uberClientID +
                "&pickup[latitude]=" + currentLat +
                "&pickup[longitude]=" + currentLong +
                "&dropoff[latitude]=" + destinationLat +
                "&dropoff[longitude]=" + destinationLong;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uberUrl));

        startActivity(intent);
    }
    public void setupUI(){

        walk = findViewById(R.id.walk);
        transit = findViewById(R.id.transit);
        ride = findViewById(R.id.ride);
        mapView = findViewById(R.id.Map);

    }

    public interface HomeAddressCallback{
        void onCallback(String address_line, String city, String province, String postal_code, String country);
    }
}