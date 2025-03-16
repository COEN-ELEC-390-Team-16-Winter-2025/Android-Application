package com.drinkwise.app.ui.soberup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drinkwise.app.databinding.FragmentSoberUpBinding;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SoberUpFragment extends Fragment {

    private FragmentSoberUpBinding binding;
    private CountDownTimer countDownTimer;

    // BAC and metabolism rate
    private double currentBac = 0.0; // Start with zero, fetch actual data later
    private static final double METABOLISM_RATE_PER_HOUR = 0.015; // Typical elimination rate

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSoberUpBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Add your GameView into the container defined in fragment_sober_up.xml
        GameView gameView = new GameView(getContext());
        binding.soberUpContainer.addView(gameView);

        // Fetch the BAC data before starting the countdown
        fetchCurrentBacAndStartCountdown();

        return root;
    }

    private void fetchCurrentBacAndStartCountdown() {
        // Example of SharedPreferences fetching
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("drinkwise_prefs", Context.MODE_PRIVATE);
        currentBac = Double.longBitsToDouble(sharedPreferences.getLong("current_bac", Double.doubleToLongBits(0.0)));

        if (currentBac <= 0.0) {
            binding.timeUntilSober.setText("You are already sober!");
        } else {
            startSoberCountdown();
        }
    }

    private void startSoberCountdown() {
        double hoursUntilSober = currentBac / METABOLISM_RATE_PER_HOUR;
        long millisUntilSober = (long) (hoursUntilSober * 60 * 60 * 1000);

        countDownTimer = new CountDownTimer(millisUntilSober, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeLeftFormatted = formatTime(millisUntilFinished);
                binding.timeUntilSober.setText("Time Until Sober: " + timeLeftFormatted);
            }

            @Override
            public void onFinish() {
                binding.timeUntilSober.setText("You are sober now!");
                saveCurrentBac(0.0); // Optional: Reset the BAC when sober
            }
        };

        countDownTimer.start();
    }

    private void saveCurrentBac(double bacValue) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("drinkwise_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("current_bac", Double.doubleToLongBits(bacValue));
        editor.apply();
    }

    private String formatTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        binding = null; // Best practice to avoid leaks
    }
}
