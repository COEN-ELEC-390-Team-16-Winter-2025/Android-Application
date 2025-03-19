package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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
        Log.d("BACEntryAdapter", "Entries set! Count: " + entries.size());

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

        // Format and set the date-time text
        holder.dateTimeTextView.setText(formatEntryDate(entry));

        // Set BAC Level with percentage
        holder.bacLevelTextView.setText(
                String.format(Locale.getDefault(), "%.2f%%", entry.getBac())
        );

        // Get Zone Status
        String zone = entry.getStatus();
        holder.zoneIndicatorTextView.setText(zone);

        // Set Text Color and Emoji based on Zone
        Context context = holder.itemView.getContext();
        int colorRes;
        String emoji;

        switch (zone) {
            case "Safe":
                colorRes = R.color.bac_safe;
                emoji = "😊";
                break;
            case "Caution":
                colorRes = R.color.bac_caution;
                emoji = "😟";
                break;
            case "Over Limit":
                colorRes = R.color.bac_danger;
                emoji = "⚠️";
                break;
            default:
                colorRes = R.color.bac_default;
                emoji = "❓";
                break;
        }

        // Apply the color to the zone text
        holder.zoneIndicatorTextView.setTextColor(
                ContextCompat.getColor(context, colorRes)
        );

        // Set the emoji text
        holder.emojiIndicatorTextView.setText(emoji);
    }

    private String formatEntryDate(BACEntry entry) {
        // Combine date and time fields into a single date-time string
        String rawDateTime = entry.getDate() + " " + entry.getTime(); // Example: "2025-03-14 22:36:39"

        Log.d("DATE_FORMAT_DEBUG", "Raw Date-Time: " + rawDateTime);

        try {
            // Input format matches the combined date-time string
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(rawDateTime);

            // Log the parsed date for debugging
            Log.d("DATE_FORMAT_DEBUG", "Parsed Date: " + date.toString());

            // Output format for display: "EEEE MMMM dd hh:mm:ss a zzz yyyy"
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE MMMM dd, yyyy - hh:mm a zzz", Locale.getDefault());
            String formattedDate = outputFormat.format(date);

            Log.d("DATE_FORMAT_DEBUG", "Formatted Date: " + formattedDate);

            return formattedDate;

        } catch (Exception e) {
            Log.e("DATE_FORMAT_DEBUG", "Parsing failed for: '" + rawDateTime + "'", e);
            return "Invalid Date: " + rawDateTime; // Fallback for invalid dates
        }
    }






    @Override
    public int getItemCount() {
        return bacEntries.size();
    }

    static class BACViewHolder extends RecyclerView.ViewHolder {
        TextView dateTimeTextView;
        TextView bacLevelTextView;
        TextView zoneIndicatorTextView;
        TextView emojiIndicatorTextView;

        public BACViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTimeTextView = itemView.findViewById(R.id.dateTime);
            bacLevelTextView = itemView.findViewById(R.id.bacLevel);
            zoneIndicatorTextView = itemView.findViewById(R.id.zoneIndicator);
            emojiIndicatorTextView = itemView.findViewById(R.id.emojiIndicator);
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
