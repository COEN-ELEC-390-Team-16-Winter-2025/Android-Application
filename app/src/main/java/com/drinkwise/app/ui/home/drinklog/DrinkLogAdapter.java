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

public class DrinkLogAdapter extends RecyclerView.Adapter<DrinkLogAdapter.ViewHolder> {

    private List<DrinkLogItem> drinkLogList;

    public DrinkLogAdapter(List<DrinkLogItem> drinkLogList) {
        this.drinkLogList = drinkLogList;
    }

    @NonNull
    @Override
    public DrinkLogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drink_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkLogAdapter.ViewHolder holder, int position) {
        DrinkLogItem item = drinkLogList.get(position);

        holder.drinkTypeTextView.setText(item.getDrinkType());
        holder.caloriesTextView.setText(item.getCalories() + " cal");
        holder.timeTextView.setText(item.getTime());

        // Set the drink image based on the drink type
        String drinkType = item.getDrinkType().toLowerCase();
        int imageResId;

        switch (drinkType) {
            case "beer":
                imageResId = R.drawable.ic_beer;
                break;
            case "wine":
                imageResId = R.drawable.ic_wine;
                break;
            case "champagne":
                imageResId = R.drawable.ic_champagne;
                break;
            case "cocktail":
                imageResId = R.drawable.ic_cocktail;
                break;
            case "sake":
                imageResId = R.drawable.ic_sake;
                break;
            case "shot":
                imageResId = R.drawable.ic_shot;
                break;
            default:
                imageResId = R.drawable.ic_beer;
                break;
        }

        holder.drinkImageView.setImageResource(imageResId);
        holder.drinkImageView.setContentDescription(item.getDrinkType() + " icon");
    }

    @Override
    public int getItemCount() {
        return drinkLogList.size();
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
            drinkImageView = itemView.findViewById(R.id.drinkImageView); // Find by ID
        }
    }
}


