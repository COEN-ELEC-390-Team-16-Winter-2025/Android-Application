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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

// Adapter class for displaying BACEntry objects in a RecyclerView.
public class BACEntryAdapter extends RecyclerView.Adapter<BACEntryAdapter.BACViewHolder> {

    // List holding all BACEntry objects to be displayed.
    private List<BACEntry> bacEntries = new ArrayList<>();

    // Method to update the BAC entries and refresh the list.
    @SuppressLint("NotifyDataSetChanged")
    public void setBacEntries(List<BACEntry> entries) {
        this.bacEntries = entries;
        Log.d("BACEntryAdapter", "Entries set! Count: " + entries.size());
        notifyDataSetChanged(); // Notify RecyclerView of data changes.
    }

    // Called to create a new ViewHolder for a BACEntry.
    @NonNull
    @Override
    public BACViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for a BAC reading.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bac_reading, parent, false);  // <-- make sure this matches your XML
        return new BACViewHolder(view);
    }

    // Called to bind data to a ViewHolder for the BACEntry at the given position.
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BACViewHolder holder, int position) {
        BACEntry entry = bacEntries.get(position);

        // Format and set the date-time text.
        holder.dateTimeTextView.setText(formatEntryDate(entry));

        // Set the BAC level text with a percentage format.
        holder.bacLevelTextView.setText(
                String.format(Locale.getDefault(), "%.2f%%", entry.getBac())
        );

        // Get the zone status (e.g., "Safe", "Impaired") and set it.
        String zone = entry.getStatus();
        holder.zoneIndicatorTextView.setText(zone);

        // Determine the appropriate text color and emoji based on the zone.
        Context context = holder.itemView.getContext();
        int colorRes;
        String emoji;

        switch (zone) {
            case "Safe":
                colorRes = R.color.bac_safe;
                emoji = "😊";
                break;
            case "Mild Impairment":
                colorRes = R.color.bac_mild_impairment;
                emoji = "😟";
                break;
            case "Impaired":
                colorRes = R.color.bac_impaired;
                emoji = "😣";
                break;
            case "High Impairment":
                colorRes = R.color.bac_high_impairment;
                emoji = "😵";
                break;
            case "Severe Impairment":
                colorRes = R.color.bac_severe_impairment;
                emoji = "🤢";
                break;
            case "Medical Emergency":
                colorRes = R.color.bac_medical_emergency;
                emoji = "🚨";
                break;
            default:
                colorRes = R.color.bac_default;
                emoji = "❓";
                break;
        }

        // Apply the determined color to the zone indicator TextView.
        holder.zoneIndicatorTextView.setTextColor(
                ContextCompat.getColor(context, colorRes)
        );

        // Set the emoji indicator text.
        holder.emojiIndicatorTextView.setText(emoji);
    }

    // Helper method to format the BACEntry's date and time into a displayable string.
    private String formatEntryDate(BACEntry entry) {
        // Combine the date and time strings from the entry.
        String rawDateTime = entry.getDate() + " " + entry.getTime(); // Example: "2025-03-14 22:36:39"
        Log.d("DATE_FORMAT_DEBUG", "Raw Date-Time: " + rawDateTime);

        try {
            // Create a SimpleDateFormat object for parsing the raw date-time string.
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(rawDateTime);

            // Log the parsed date.
            Log.d("DATE_FORMAT_DEBUG", "Parsed Date: " + Objects.requireNonNull(date));

            // Create another SimpleDateFormat for the output format.
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE MMMM dd, yyyy - hh:mm a zzz", Locale.getDefault());
            String formattedDate = outputFormat.format(date);

            Log.d("DATE_FORMAT_DEBUG", "Formatted Date: " + formattedDate);
            return formattedDate;

        } catch (Exception e) {
            // Log an error if parsing fails and return a fallback string.
            Log.e("DATE_FORMAT_DEBUG", "Parsing failed for: '" + rawDateTime + "'", e);
            return "Invalid Date: " + rawDateTime;
        }
    }

    // Returns the total number of BAC entries.
    @Override
    public int getItemCount() {
        return bacEntries.size();
    }

    // ViewHolder class that holds the views for a single BACEntry item.
    public static class BACViewHolder extends RecyclerView.ViewHolder {
        // TextView for displaying formatted date and time.
        TextView dateTimeTextView;
        // TextView for displaying the BAC level.
        TextView bacLevelTextView;
        // TextView for displaying the status (zone).
        TextView zoneIndicatorTextView;
        // TextView for displaying an emoji based on the status.
        TextView emojiIndicatorTextView;

        // Constructor that initializes the view references.
        public BACViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTimeTextView = itemView.findViewById(R.id.dateTime);
            bacLevelTextView = itemView.findViewById(R.id.bacLevel);
            zoneIndicatorTextView = itemView.findViewById(R.id.zoneIndicator);
            emojiIndicatorTextView = itemView.findViewById(R.id.emojiIndicator);
        }
    }

    // Helper method to determine the zone based on a given BAC value.
    // (This method is not used in onBindViewHolder but is available for other uses.)
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