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
    private static final int TYPE_SEPARATOR = 3;

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
        NotificationItem item =  itemList.get(position);
        if(item instanceof SeparatorItem) {
            return TYPE_SEPARATOR;
        } else {
            return itemList.get(position).getType();
        }

    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == TYPE_SEPARATOR) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_separator, parent, false);
            return new SeparatorViewHolder(view);
        }
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
        if(holder instanceof SeparatorViewHolder) {
            ((SeparatorViewHolder) holder).bind(item);
        } else {
            holder.bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateData(List<NotificationItem> newList) {
        this.itemList = newList;
        //sortList();
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

    public static class SeparatorViewHolder extends NotifViewHolder {
        TextView separatorLabel;

        public SeparatorViewHolder(@NonNull View itemView) {
            super(itemView);
            separatorLabel = itemView.findViewById(R.id.separatorLabel);
        }

        //@Override
        public void bind(NotificationItem sep) {
            separatorLabel.setText("New messages");
        }
    }













}
