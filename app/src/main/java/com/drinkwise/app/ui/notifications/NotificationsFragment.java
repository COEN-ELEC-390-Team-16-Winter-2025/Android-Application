package com.drinkwise.app.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.drinkwise.app.R;
import com.drinkwise.app.databinding.FragmentNotificationsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView recyclerView;
    private NotifAdapter notifAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;


        // Initialize RecyclerView
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get notifications from Firestore (or another source)
        List<ReminderItem> reminderList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Assuming userId is already available
        db.collection("users")
                .document(userId)  // Point to the specific user document
                .collection("reminders")  // Fetch from the "reminders" subcollection of that user
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            // Convert the document to a ReminderItem object
                            ReminderItem reminderItem = document.toObject(ReminderItem.class);
                            reminderList.add(reminderItem);
                        }

                        // Log the fetched reminders
                        Log.d("NotificationsFragment", "Fetched " + reminderList.size() + " reminders");

                        // Set up the adapter with the reminder data
                        notifAdapter = new NotifAdapter(reminderList);
                        recyclerView.setAdapter(notifAdapter);
                    } else {
                        Log.e("NotificationsFragment", "Error fetching reminders: ", task.getException());
                    }
                });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
