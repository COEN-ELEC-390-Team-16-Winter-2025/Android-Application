package com.drinkwise.app.ui.home.drinklog;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drinkwise.app.R;

import java.util.List;
import java.util.Locale;

// Adapter class for displaying a list of drink log entries in a RecyclerView
public class DrinkLogAdapter extends RecyclerView.Adapter<DrinkLogAdapter.ViewHolder> {

    private List<DrinkLogItem> drinkLogList;

    public DrinkLogAdapter(List<DrinkLogItem> drinkLogList) {
        this.drinkLogList = drinkLogList;
    }

    // This is called when RecyclerView needs a new ViewHolder of the given type
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drink_log, parent, false);
        return new ViewHolder(view);
    }

    // This connects data to the ViewHolder at a specified position
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DrinkLogItem item = drinkLogList.get(position);
        if (item.getDrinkType() != null) {
            holder.drinkTypeTextView.setText(item.getDrinkType());
        } else {
            holder.drinkTypeTextView.setText("Unknown Drink");
        }

        if (item.getCalories() != null) {
            holder.caloriesTextView.setText(String.format(Locale.getDefault(), "%d cal", item.getCalories()));
        } else {
            holder.caloriesTextView.setText("N/A");
        }

        holder.timeTextView.setText(item.getTime());

        int imageResId = getDrinkImageResource(item.getDrinkType());
        holder.drinkImageView.setImageResource(imageResId);
        holder.drinkImageView.setContentDescription(item.getDrinkType() + " icon");
    }

    @Override
    public int getItemCount() {
        return drinkLogList.size();
    }

    // updates the adapter's dataset and notify the RecyclerView to refresh
    @SuppressLint("NotifyDataSetChanged")
    public void setDrinkLogEntries(List<DrinkLogItem> entries) {
        this.drinkLogList = entries;
        notifyDataSetChanged();
    }

    // returns an image for each drink type
    private int getDrinkImageResource(String drinkType) {
        if (drinkType == null) {
            return R.drawable.ic_beer;
        }
        switch (drinkType.toLowerCase()) {
            case "wine":
                return R.drawable.ic_wine;
            case "champagne":
                return R.drawable.ic_champagne;
            case "cocktail":
                return R.drawable.ic_cocktail;
            case "sake":
                return R.drawable.ic_sake;
            case "shot":
                return R.drawable.ic_shot;
            case "beer":
                return R.drawable.ic_beer;
            default:
                return R.drawable.ic_custom;
        }
    }

    // holds the view elements for each drink log item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView drinkTypeTextView;
        TextView caloriesTextView;
        TextView timeTextView;
        ImageView drinkImageView;

        // finds and assigns view references from the item layout
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkTypeTextView = itemView.findViewById(R.id.drinkTypeTextView);
            caloriesTextView = itemView.findViewById(R.id.caloriesTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            drinkImageView = itemView.findViewById(R.id.drinkImageView);
        }
    }
}