package com.drinkwise.app.ui.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drinkwise.app.R;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.NotifViewHolder> {

    private List<NotificationItem> itemList;
    private static final int TYPE_REMINDER = 0;
    private static final int TYPE_RECOMMENDATION = 1;
    private static final int TYPE_ALERT = 2;
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
        } else if (viewType == TYPE_RECOMMENDATION) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
            return new RecommendationViewHolder(view);
        } else if (viewType == TYPE_ALERT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
            return new AlertViewHolder(view);
        }
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

    public abstract static class NotifViewHolder extends RecyclerView.ViewHolder {
        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public abstract void bind(NotificationItem item);
    }


    public static class ReminderViewHolder extends NotifViewHolder {
        TextView reminderMessage, reminderTimestamp, reminderTypeTextView, escalationTextView, intervalTextView;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            reminderMessage = itemView.findViewById(R.id.reminderMessageTextView);
            reminderTimestamp = itemView.findViewById(R.id.reminderTimestampTextView);
            reminderTypeTextView = itemView.findViewById(R.id.reminderTypeTextView);
            escalationTextView = itemView.findViewById(R.id.reminderEscalationTextView);
            intervalTextView = itemView.findViewById(R.id.reminderIntervalTextView);
        }

        @Override
        public void bind(NotificationItem reminderTemp) {
            // Ensure this is actually a ReminderItem before casting
            if (!(reminderTemp instanceof ReminderItem)) return;

            ReminderItem reminder = (ReminderItem) reminderTemp;

            Log.d("ReminderViewHolder", "Reminder: " + reminder.getMessage() +
                    " | Interval: " + reminder.getInterval() +
                    " | Escalation: " + reminder.getEscalation() +
                    " | Timestamp: " + (reminder.getTimestamp() != null ? reminder.getTimestamp().toDate().toString() : "null"));


            reminderMessage.setText(reminder.getMessage());
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            reminderTimestamp.setText(sdf.format(reminder.getTimestamp().toDate()));
            intervalTextView.setText("Interval: " + String.valueOf(reminder.getInterval()) + " minutes" );
            escalationTextView.setText("Escalation: " + String.valueOf(reminder.getEscalation()));


        }
    }

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


    public static class AlertViewHolder extends NotifViewHolder {
        TextView alertMessageTextView, alertTimestampTextView, alertTypeTextView, alertBacValueTextView, alertSafetyLevelTextView;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            alertMessageTextView = itemView.findViewById(R.id.alertMessage);
            alertTimestampTextView = itemView.findViewById(R.id.alertTimestamp);
            alertTypeTextView = itemView.findViewById(R.id.alertType);
            alertBacValueTextView = itemView.findViewById(R.id.alertBacValue);
            alertSafetyLevelTextView = itemView.findViewById(R.id.alertSafetyLevel);
        }

        @Override
        public void bind(NotificationItem item) {
            if (item instanceof AlertItem) {
                AlertItem alert = (AlertItem) item;


                // Message
                alertMessageTextView.setText(alert.getMessage() != null ? alert.getMessage() : "No message");

                // Timestamp
                if (alert.getTimestamp() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                    alertTimestampTextView.setText(sdf.format(alert.getTimestamp().toDate()));
                } else {
                    alertTimestampTextView.setText("No timestamp");
                }

                // Alert Type (optional)
                alertTypeTextView.setText(alert.getAlertType() != null ? alert.getAlertType() : "Alert");

                // BAC
                alertBacValueTextView.setText("BAC: " + alert.getBacValue());


                // Safety Level
                alertSafetyLevelTextView.setText("Safety: " + (alert.getSafetyLevel() != null ? alert.getSafetyLevel() : "Unknown"));
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

