package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BACEntryAdapter extends RecyclerView.Adapter<BACEntryAdapter.BACViewHolder> {

    private List<BACEntry> bacEntries = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setBacEntries(List<BACEntry> entries) {
        this.bacEntries = entries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BACViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bac_reading, parent, false);  // <-- make sure this matches your XML
        return new BACViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BACViewHolder holder, int position) {
        BACEntry entry = bacEntries.get(position);

        // Use the date and time from the entry directly
        String dateTime = entry.getDate() + " - " + entry.getTime();
        holder.dateTimeTextView.setText(dateTime);

        // Show BAC value (as percentage)
        holder.bacLevelTextView.setText(String.format(Locale.getDefault(), "%.2f%%", entry.getBac()));

        // Get status (Safe, Caution, Over Limit) directly
        String zone = entry.getStatus();
        holder.zoneIndicatorTextView.setText(zone);

        // Optional: Change background color based on zone/status
        int colorRes;
        switch (zone) {
            case "Safe":
                colorRes = R.color.bac_safe;
                break;
            case "Caution":
                colorRes = R.color.bac_caution;
                break;
            case "Over Limit":
                colorRes = R.color.bac_danger;
                break;
            default:
                colorRes = R.color.bac_default;
                break;
        }

        holder.zoneIndicatorTextView.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), colorRes)
        );
    }


    @Override
    public int getItemCount() {
        return bacEntries.size();
    }

    static class BACViewHolder extends RecyclerView.ViewHolder {
        TextView dateTimeTextView;
        TextView bacLevelTextView;
        TextView zoneIndicatorTextView;

        public BACViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTimeTextView = itemView.findViewById(R.id.dateTime);
            bacLevelTextView = itemView.findViewById(R.id.bacLevel);
            zoneIndicatorTextView = itemView.findViewById(R.id.zoneIndicator);
        }
    }

    private String getZoneForBac(double bac) {
        if (bac < 0.03) {
            return "Safe";
        } else if (bac < 0.08) {
            return "Caution";
        } else {
            return "Danger";
        }
    }
}
