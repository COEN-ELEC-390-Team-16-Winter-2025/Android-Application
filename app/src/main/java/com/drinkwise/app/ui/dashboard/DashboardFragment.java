package com.drinkwise.app.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.drinkwise.app.databinding.FragmentDashboardBinding;


import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.drinkwise.app.R;
import com.drinkwise.app.ScanningActivity;

public class DashboardFragment extends Fragment {

private FragmentDashboardBinding binding;
private TextView latest_bac_measurement;
private Button refresh_bac;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.btnSeeList.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScanningActivity.class);
            intent.putExtra("mode", "fullList"); // Mode for full BAC list
            startActivity(intent);
        });

        binding.btnRefreshBAC.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScanningActivity.class);
            intent.putExtra("mode", "refreshBac"); // Mode for refreshing BAC
            startActivity(intent);
        });



//        final TextView textView = binding.textDashboard;
//        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        latest_bac_measurement = view.findViewById(R.id.latest_BAC_measurement);
        refresh_bac = view.findViewById(R.id.btnRefreshBAC);

        if(getArguments() != null){
            latest_bac_measurement.setText(getArguments().getString("latest_bac_entry"));

        }


        refresh_bac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ScanningActivity.class);
                intent.putExtra("mode", "refreshBAC");
                startActivity(intent);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}