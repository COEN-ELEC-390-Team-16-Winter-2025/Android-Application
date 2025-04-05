package com.drinkwise.app.ui.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drinkwise.app.R;

import com.google.firebase.Timestamp;

import org.checkerframework.checker.units.qual.N;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.NotifViewHolder> {

    private List<NotificationItem> itemList;
    private static final int TYPE_REMINDER = 0;
    private static final int TYPE_RECOMMENDATION = 1;

    public NotifAdapter(Context context, List<NotificationItem> itemList) {
        this.itemList = itemList;
        sortList();
    }

    private void sortList() {
        Collections.sort(itemList, new Comparator<NotificationItem>() {
            @Override
            public int compare(NotificationItem o1, NotificationItem o2) {
                return Long.compare(o2.getTimestampMillis(), o1.getTimestampMillis());
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == TYPE_REMINDER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
            return new ReminderViewHolder(view);
        } else if(viewType == TYPE_RECOMMENDATION) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
            return new RecommendationViewHolder(view);
        }
        // Fallback: default to reminder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        NotificationItem item = itemList.get(position);
       holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateData(List<NotificationItem> newList) {
        this.itemList = newList;
        sortList();
        notifyDataSetChanged();
    }

    //
    public abstract static class NotifViewHolder extends RecyclerView.ViewHolder {
        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public abstract void bind(NotificationItem item);
    }


    // Reminder ViewHolder
    public static class ReminderViewHolder extends NotifViewHolder {
        TextView reminderMessage, reminderTimestamp, reminderTypeTextView;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            reminderMessage = itemView.findViewById(R.id.reminderMessageTextView);
            reminderTimestamp = itemView.findViewById(R.id.reminderTimestampTextView);
            reminderTypeTextView = itemView.findViewById(R.id.reminderTypeTextView);
        }

        @Override
        public void bind(NotificationItem reminder) {
            reminderMessage.setText(reminder.getMessage());
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            reminderTimestamp.setText(sdf.format(reminder.getTimestamp().toDate()));
        }
    }

    // Recommendation ViewHolder
    public static class RecommendationViewHolder extends NotifViewHolder {
        TextView recommendationMessageTextView, recommendationTimestampTextView, recommendationTypeTextView;

        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            recommendationMessageTextView = itemView.findViewById(R.id.recommendationMessageTextView);
            recommendationTimestampTextView = itemView.findViewById(R.id.recommendationTimestampTextView);
            recommendationTypeTextView = itemView.findViewById(R.id.recommendationTypeTextView);
        }

        public void bind(NotificationItem rec) {
            recommendationMessageTextView.setText(rec.getMessage());
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            if (rec.getTimestamp() != null) {
                recommendationTimestampTextView.setText(sdf.format(rec.getTimestamp().toDate()));
            } else {
                recommendationTimestampTextView.setText("No timestamp");
            }
        }
    }






















    /*
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

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item (item_reminder.xml)
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new NotifViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(NotifViewHolder holder, int position) {
        // Get the current reminder item
        ReminderItem reminderItem = reminderList.get(position);

        // Bind the data to the views in the ViewHolder
        holder.reminderTypeTextView.setText(reminderItem.getReminderType());
        holder.messageTextView.setText(reminderItem.getMessage());

        // Format the timestamp (convert Timestamp to a readable format)
        Timestamp timestamp = reminderItem.getTimestamp();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTimestamp = sdf.format(new Date(timestamp.getSeconds() * 1000));
        holder.timestampTextView.setText(formattedTimestamp);

        // Display interval and escalation if available
        holder.intervalTextView.setText("Interval: " + reminderItem.getIntervalMinutes() + " minutes");
        holder.escalationTextView.setText("Escalation: " + reminderItem.getEscalation());
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }*/

}
