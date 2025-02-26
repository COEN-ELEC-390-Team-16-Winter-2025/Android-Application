package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
                .inflate(R.layout.item_bac, parent, false);
        return new BACViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BACViewHolder holder, int position) {
        BACEntry entry = bacEntries.get(position);

        // Format timestamp
        Timestamp ts = entry.getTimestamp();
        Date date = (ts != null) ? ts.toDate() : new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String dateString = sdf.format(date);

        // Show BAC value and timestamp
        holder.bacTextView.setText(String.format(Locale.getDefault(), "BAC: %.3f", entry.getBac()));
        holder.timestampTextView.setText("Time: " + dateString);
    }

    @Override
    public int getItemCount() {
        return bacEntries.size();
    }

    static class BACViewHolder extends RecyclerView.ViewHolder {
        TextView bacTextView;
        TextView timestampTextView;

        public BACViewHolder(@NonNull View itemView) {
            super(itemView);
            bacTextView = itemView.findViewById(R.id.bacTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}