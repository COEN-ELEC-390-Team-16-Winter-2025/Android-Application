package com.drinkwise.app.ui.home.drinklog;

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

public class DrinkLogAdapter extends RecyclerView.Adapter<DrinkLogAdapter.ViewHolder> {

    private List<DrinkLogItem> drinkLogList;

    public DrinkLogAdapter(List<DrinkLogItem> drinkLogList) {
        this.drinkLogList = drinkLogList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drink_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DrinkLogItem item = drinkLogList.get(position);

        // Set drink type
        if (item.getDrinkType() != null) {
            holder.drinkTypeTextView.setText(item.getDrinkType());
        } else {
            holder.drinkTypeTextView.setText("Unknown Drink");
        }

        // Set calories
        if (item.getCalories() != null) {
            holder.caloriesTextView.setText(String.format(Locale.getDefault(), "%d cal", item.getCalories()));
        } else {
            holder.caloriesTextView.setText("N/A");
        }

        // Set time
        holder.timeTextView.setText(item.getTime());

        // Set drink image
        int imageResId = getDrinkImageResource(item.getDrinkType());
        holder.drinkImageView.setImageResource(imageResId);
        holder.drinkImageView.setContentDescription(item.getDrinkType() + " icon");
    }

    @Override
    public int getItemCount() {
        return drinkLogList.size();
    }

    // method to update the dataset
    public void setDrinkLogEntries(List<DrinkLogItem> entries) {
        this.drinkLogList = entries;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    private int getDrinkImageResource(String drinkType) {
        if (drinkType == null) {
            return R.drawable.ic_beer;
        }
        switch (drinkType.toLowerCase()) {
            case "beer": return R.drawable.ic_beer;
            case "wine": return R.drawable.ic_wine;
            case "champagne": return R.drawable.ic_champagne;
            case "cocktail": return R.drawable.ic_cocktail;
            case "sake": return R.drawable.ic_sake;
            case "shot": return R.drawable.ic_shot;
            default: return R.drawable.ic_beer;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView drinkTypeTextView;
        TextView caloriesTextView;
        TextView timeTextView;
        ImageView drinkImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkTypeTextView = itemView.findViewById(R.id.drinkTypeTextView);
            caloriesTextView = itemView.findViewById(R.id.caloriesTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            drinkImageView = itemView.findViewById(R.id.drinkImageView);
        }
    }
}