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
    private NotifAdapter NotfiAdapter;
    private List<NotificationItem> recommendationList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "RecommendationsFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recommendations, container, false);
        recyclerView = root.findViewById(R.id.recyclerViewRecommendation);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        NotfiAdapter = new NotifAdapter(getContext(), recommendationList);
        recyclerView.setAdapter(NotfiAdapter);
        loadRecommendations();
        return root;
    }

    private void loadRecommendations() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if(userId == null) {
            Log.e(TAG, "No user logged in");
            return;
        }
        db.collection("users")
                .document(userId)
                .collection("Recommendations")
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        recommendationList.clear();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            RecommendationItem rec = document.toObject(RecommendationItem.class);
                            recommendationList.add(rec);
                        }
                        Log.d(TAG, "Fetched " + recommendationList.size() + "recommendations");
                    } else {
                        Log.e(TAG, "Error fetching recommendations: ", task.getException());
                    }
                });
    }

}
