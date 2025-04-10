package com.drinkwise.app.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.drinkwise.app.R;
import com.drinkwise.app.databinding.FragmentNotificationsBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationsFragment extends Fragment {

    //private RecyclerView recyclerView;
    //private NotifAdapter notifAdapter;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        tabLayout = root.findViewById(R.id.tabLayout);
        viewPager = root.findViewById(R.id.viewPager);

        //Get preferences from firestore and then set up the ViewPager
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if(userId != null) {
            db.collection("users")
                    .document(userId)
                    .collection("profile")
                    .document("Preferences")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        boolean showRecommendations = documentSnapshot.getBoolean("Recommendations") != null ?
                                documentSnapshot.getBoolean("Recommendations") : true;
                        boolean showReminders = documentSnapshot.getBoolean("Reminders") != null ?
                                documentSnapshot.getBoolean("Reminders") : true;
                        boolean showAlerts = documentSnapshot.getBoolean("Alerts") != null ?
                                documentSnapshot.getBoolean("Alerts") : true;



                        setupViewPager(viewPager, showReminders, showRecommendations, showAlerts);
                        tabLayout.setupWithViewPager(viewPager);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NotificationsFragment", "Error fetching preferences: ", e);
                        setupViewPager(viewPager, true, true, true);
                        tabLayout.setupWithViewPager(viewPager);
                    });
        } else {
            setupViewPager(viewPager, true, true, true);
            tabLayout.setupWithViewPager(viewPager);
        }
        return root;
    }

    //private void setupViewPager(ViewPager viewPager, boolean showReminders, boolean showRecommendations, boolean showAlerts)
    private void setupViewPager(ViewPager viewPager, boolean showReminders, boolean showRecommendations,  boolean showAlerts) {
        NotificationsPagerAdapter adapter = new NotificationsPagerAdapter(getChildFragmentManager());

        RemindersFragment remindersFragment =  new RemindersFragment();
        Bundle remArgs = new Bundle();
        remArgs.putBoolean("showReminders", showReminders);
        remindersFragment.setArguments(remArgs);

        RecommendationsFragment recommendationsFragment =  new RecommendationsFragment();
        Bundle recArgs = new Bundle();
        recArgs.putBoolean("showRecommendations", showRecommendations);
        recommendationsFragment.setArguments(recArgs);


        AlertsFragment alertFragment =  new AlertsFragment();
        Bundle alertArgs = new Bundle();
        alertArgs.putBoolean("showAlerts", showAlerts);
        alertFragment.setArguments(alertArgs);





        adapter.addFragment(remindersFragment, "Reminders");
        adapter.addFragment(recommendationsFragment, "Recommendations");
        adapter.addFragment(new AlertsFragment(), "Alerts");


        viewPager.setAdapter(adapter);
    }


}
