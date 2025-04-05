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

    // List that holds DrinkLogItem objects, each representing a logged drink entry
    private List<DrinkLogItem> drinkLogList;

    // Constructor initializes the adapter with a list of DrinkLogItem objects
    public DrinkLogAdapter(List<DrinkLogItem> drinkLogList) {
        this.drinkLogList = drinkLogList;
    }

    // Called when RecyclerView needs a new ViewHolder of the given type.
    // Inflates the item_drink_log layout to create a new view.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout from XML
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drink_log, parent, false);
        return new ViewHolder(view);
    }

    // Called to bind data to the ViewHolder at a specified position.
    // Uses the data from the DrinkLogItem at the given position to populate the views.
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the DrinkLogItem for this position
        DrinkLogItem item = drinkLogList.get(position);

        // Set drink type text : if null, display "Unknown Drink"
        if (item.getDrinkType() != null) {
            holder.drinkTypeTextView.setText(item.getDrinkType());
        } else {
            holder.drinkTypeTextView.setText("Unknown Drink");
        }

        // Set calories text using String.format with default locale :
        // if calories is null, display "N/A"
        if (item.getCalories() != null) {
            holder.caloriesTextView.setText(String.format(Locale.getDefault(), "%d cal", item.getCalories()));
        } else {
            holder.caloriesTextView.setText("N/A");
        }

        // Set the time at which the drink was logged
        holder.timeTextView.setText(item.getTime());

        // Determine and set the image resource based on the drink type
        int imageResId = getDrinkImageResource(item.getDrinkType());
        holder.drinkImageView.setImageResource(imageResId);
        holder.drinkImageView.setContentDescription(item.getDrinkType() + " icon");
    }

    // Returns the total number of items in the dataset
    @Override
    public int getItemCount() {
        return drinkLogList.size();
    }

    // Method to update the adapter's dataset and notify the RecyclerView to refresh
    @SuppressLint("NotifyDataSetChanged")
    public void setDrinkLogEntries(List<DrinkLogItem> entries) {
        this.drinkLogList = entries;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    // Helper method to return an appropriate image resource based on the drink type
    private int getDrinkImageResource(String drinkType) {
        if (drinkType == null) {
            return R.drawable.ic_beer;
        }
        // Use a switch statement to select the image resource based on the lower-case drink type
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

    // ViewHolder class that holds the view elements for each drink log item.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // TextViews for displaying drink type, calories, and time
        TextView drinkTypeTextView;
        TextView caloriesTextView;
        TextView timeTextView;
        // ImageView for displaying the drink image
        ImageView drinkImageView;

        // Constructor: find and assign view references from the item layout
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkTypeTextView = itemView.findViewById(R.id.drinkTypeTextView);
            caloriesTextView = itemView.findViewById(R.id.caloriesTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            drinkImageView = itemView.findViewById(R.id.drinkImageView);
        }
    }
}