package com.drinkwise.app.ui.notifications;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drinkwise.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotifAdapter notifAdapter;
    private List<NotificationItem> recommendationList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "RecommendationsFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recommendations, container, false);
        recyclerView = root.findViewById(R.id.recyclerViewRecommendation);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notifAdapter = new NotifAdapter(getContext(), recommendationList);
        recyclerView.setAdapter(notifAdapter);

        boolean showRecommendations = true;
        if (getArguments() != null) {
            showRecommendations = getArguments().getBoolean("showRecommendations", true);
        }

        if (showRecommendations) {
            loadRecommendations();
        } else {
            recommendationList.clear();
            notifAdapter.updateData(recommendationList);
        }

        return root;
    }

    private void loadRecommendations() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.e(TAG, "No user logged in");
            return;
        }

        //Fetch the preferences
        db.collection("users")
                .document(userId)
                .collection("profile")
                .document("Preferences")
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error fetching preferences: ", e);
                        return;
                    }
                    boolean recommendationsEnabled = false;
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Boolean recommendationPref = documentSnapshot.getBoolean("Recommendations");
                        recommendationsEnabled = (recommendationPref != null) ? recommendationPref : false;
                    }
                    if (!recommendationsEnabled) {
                        recommendationList.clear();
                        notifAdapter.updateData(recommendationList);
                        Log.d(TAG, "Recommendations are disabled");
                    } else {

                        //Fetch the the recommendations
                        db.collection("users")
                                .document(userId)
                                .collection("Recommendations")
                                .orderBy("Timestamp", Query.Direction.DESCENDING)
                                .addSnapshotListener((querySnapshot, queryError) -> {
                                    if (queryError != null) {
                                        Log.e(TAG, "Error fetching recommendations: ", queryError);
                                        return;
                                    }
                                    if (querySnapshot != null) {
                                        recommendationList.clear();
                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                            RecommendationItem rec = document.toObject(RecommendationItem.class);
                                            recommendationList.add(rec);
                                        }
                                        Log.d(TAG, "Fetched " + recommendationList.size() + "recommendations");
                                        notifAdapter.updateData(recommendationList);
                                    }
                                });
                    }

                });
    }
}
