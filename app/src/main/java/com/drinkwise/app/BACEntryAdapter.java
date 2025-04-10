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

// Adapter class for displaying BACEntry objects in a RecyclerView
public class BACEntryAdapter extends RecyclerView.Adapter<BACEntryAdapter.BACViewHolder> {

    private List<BACEntry> bacEntries = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setBacEntries(List<BACEntry> entries) {
        this.bacEntries = entries;
        Log.d("BACEntryAdapter", "Entries set! Count: " + entries.size());
        notifyDataSetChanged();
    }

    // ViewHolder for a BACEntry
    @NonNull
    @Override
    public BACViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bac_reading, parent, false);
        return new BACViewHolder(view);
    }

    // connects data to ViewHolder for the BACEntry at given position.
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BACViewHolder holder, int position) {
        BACEntry entry = bacEntries.get(position);
        holder.dateTimeTextView.setText(formatEntryDate(entry));
        holder.bacLevelTextView.setText(
                String.format(Locale.getDefault(), "%.2f%%", entry.getBac())
        );

        String zone = entry.getStatus();
        holder.zoneIndicatorTextView.setText(zone);

        // different emoji based on zone
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

        holder.zoneIndicatorTextView.setTextColor(
                ContextCompat.getColor(context, colorRes)
        );
        holder.emojiIndicatorTextView.setText(emoji);
    }

    // method to format BACEntry's date and time into a displayable string.
    private String formatEntryDate(BACEntry entry) {
        String rawDateTime = entry.getDate() + " " + entry.getTime();
        Log.d("DATE_FORMAT_DEBUG", "Raw Date-Time: " + rawDateTime);

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(rawDateTime);

            Log.d("DATE_FORMAT_DEBUG", "Parsed Date: " + Objects.requireNonNull(date));

            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE MMMM dd, yyyy - hh:mm a zzz", Locale.getDefault());
            String formattedDate = outputFormat.format(date);

            Log.d("DATE_FORMAT_DEBUG", "Formatted Date: " + formattedDate);
            return formattedDate;

        } catch (Exception e) {
            Log.e("DATE_FORMAT_DEBUG", "Parsing failed for: '" + rawDateTime + "'", e);
            return "Invalid Date: " + rawDateTime;
        }
    }

    @Override
    public int getItemCount() {
        return bacEntries.size();
    }

    // holds views for BACEntry item.
    public static class BACViewHolder extends RecyclerView.ViewHolder {
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

}