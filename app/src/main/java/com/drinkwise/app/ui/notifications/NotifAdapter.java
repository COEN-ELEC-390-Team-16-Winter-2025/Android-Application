package com.drinkwise.app.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.drinkwise.app.R;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.NotifViewHolder> {

    private List<ReminderItem> reminderList;

    // ViewHolder class to hold the views for each notification item
    public static class NotifViewHolder extends RecyclerView.ViewHolder {
        public TextView reminderTypeTextView;
        public TextView messageTextView;
        public TextView timestampTextView;
        public TextView intervalTextView;
        public TextView escalationTextView;

        public NotifViewHolder(View itemView) {
            super(itemView);
            reminderTypeTextView = itemView.findViewById(R.id.reminderTypeTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            intervalTextView = itemView.findViewById(R.id.intervalTextView);
            escalationTextView = itemView.findViewById(R.id.escalationTextView);
        }
    }

    // Constructor for the adapter
    public NotifAdapter(List<ReminderItem> reminders) {
        this.reminderList = reminders;
    }

    @Override
    public NotifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item (item_reminder.xml)
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new NotifViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NotifViewHolder holder, int position) {
        // Get the current reminder item
        ReminderItem reminderItem = reminderList.get(position);

        // Bind the data to the views in the ViewHolder
        holder.reminderTypeTextView.setText(reminderItem.getReminderType());
        holder.messageTextView.setText(reminderItem.getMessage());

        // Format the timestamp (convert Timestamp to a readable format)
        Timestamp timestamp = reminderItem.getTimestamp();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTimestamp = sdf.format(new Date(timestamp.getSeconds() * 1000));
        holder.timestampTextView.setText(formattedTimestamp);

        // Display interval and escalation if available
        holder.intervalTextView.setText("Interval: " + reminderItem.getIntervalMinutes() + " minutes");
        holder.escalationTextView.setText("Escalation: " + reminderItem.getEscalation());
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }
}
