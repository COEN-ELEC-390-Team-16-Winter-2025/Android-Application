package com.drinkwise.app;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ScanningAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<BACEntry> bacEntries;
    private LayoutInflater inflater;

    public ScanningAdapter(Context context, ArrayList<BACEntry> bacEntries) {
        this.context = context;
        this.bacEntries = bacEntries;
        inflater = LayoutInflater.from(context);

        // Sort the list initially
        sortBACEntriesByTimestamp();
    }

    @Override
    public int getCount() {
        return bacEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.bac_list_view, null);

        TextView bac_value = convertView.findViewById(R.id.bac_value);
        TextView bac_status_holder = convertView.findViewById(R.id.bac_status_holder);
        TextView bac_status = convertView.findViewById(R.id.bac_status);
        TextView bac_date = convertView.findViewById(R.id.bac_date);
        TextView bac_time = convertView.findViewById(R.id.bac_time);
        sortBACEntriesByTimestamp();
        double bac = bacEntries.get(position).getBac();
        bac_value.setText("BAC Value: " + String.format("%.3f", bac));
        bac_status_holder.setText("Status: ");
        bac_status.setText(bacEntries.get(position).getStatus());
        bac_date.setText("Date: " + bacEntries.get(position).getDate());
        bac_time.setText("Time: " + bacEntries.get(position).getTime());

        // Set color based on status
        switch (bacEntries.get(position).getStatus().trim()) {
            case "Safe":
                bac_status.setTextColor(ContextCompat.getColor(context, R.color.bac_safe));
                break;
            case "Mild Impairment":
                bac_status.setTextColor(ContextCompat.getColor(context, R.color.bac_mild_impairment));
                break;
            case "Impaired":
                bac_status.setTextColor(ContextCompat.getColor(context, R.color.bac_impaired));
                break;
            case "High Impairment":
                bac_status.setTextColor(ContextCompat.getColor(context, R.color.bac_high_impairment));
                break;
            case "Severe Impairment":
                bac_status.setTextColor(ContextCompat.getColor(context, R.color.bac_severe_impairment));
                break;
            case "Medical Emergency":
                bac_status.setTextColor(ContextCompat.getColor(context, R.color.bac_medical_emergency));
                break;
            default:
                bac_status.setTextColor(ContextCompat.getColor(context, R.color.bac_default));
                break;
        }

        return convertView;
    }

    // Method to add a new BAC entry dynamically
    public void addNewBACEntry(BACEntry newEntry) {
        bacEntries.add(newEntry); // Add new BAC entry
        Log.d("ScanningAdapter", "Added new entry: " + newEntry.getTime());

        // Sort the list again
        sortBACEntriesByTimestamp();

        // Notify the adapter that the data has changed
        notifyDataSetChanged(); // Update the list view
    }

    private void sortBACEntriesByTimestamp() {
        // Sort the BAC entries by timestamp (most recent first)
        Log.d("ScanningAdapter", "Sorting BAC entries by timestamp.");

        Collections.sort(bacEntries, new Comparator<BACEntry>() {
            @Override
            public int compare(BACEntry entry1, BACEntry entry2) {
                // Compare timestamps in descending order (most recent first)
                return entry2.getTimestamp().compareTo(entry1.getTimestamp()); // Swapped here
            }
        });

        // Log the sorted list
        Log.d("ScanningAdapter", "Sorted BAC entries by timestamp:");
        for (BACEntry entry : bacEntries) {
            Log.d("ScanningAdapter", "Timestamp: " + entry.getTimestamp());
        }
    }

}
