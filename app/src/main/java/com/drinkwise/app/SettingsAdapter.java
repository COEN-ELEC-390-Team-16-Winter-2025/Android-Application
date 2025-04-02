package com.drinkwise.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// Adapter for displaying EmergencyContact objects in a RecyclerView.
public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder>{

    // List of emergency contacts to display.
    private ArrayList<EmergencyContact> emergency_contacts;

    // ViewHolder class holds the view references for each item in the list.
    public static class ViewHolder extends RecyclerView.ViewHolder{

        // TextViews to show contact details.
        TextView name, phone_no, email, relationship;
        // ImageView for the contact's profile picture.
        ImageView profile_picture;
        // Layouts for grouping views and toggling additional details.
        LinearLayout recycler_layout, phone_no_layout, email_layout, relationship_layout;

        // Constructor that initializes the view references.
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by ID.
            name = itemView.findViewById(R.id.name_recycler);
            phone_no = itemView.findViewById(R.id.phone_no_recycler);
            email = itemView.findViewById(R.id.email_recycler);
            relationship = itemView.findViewById(R.id.relationship_recycler);

            recycler_layout = itemView.findViewById(R.id.recycler_layout);
            phone_no_layout = itemView.findViewById(R.id.phone_no_layout_recycler);
            email_layout = itemView.findViewById(R.id.email_layout_recycler);
            relationship_layout = itemView.findViewById(R.id.relationship_layout_recycler);
        }

        public TextView getName() {
            return name;
        }

        public TextView getPhone_no() {
            return phone_no;
        }

        public TextView getEmail() {
            return email;
        }

        public TextView getRelationship() {
            return relationship;
        }

        public ImageView getProfile_picture() {
            return profile_picture;
        }

        public LinearLayout getPhone_no_layout() {
            return phone_no_layout;
        }

        public LinearLayout getEmail_layout() {
            return email_layout;
        }

        public LinearLayout getRelationship_layout() {
            return relationship_layout;
        }

        public LinearLayout getRecycler_layout() {
            return recycler_layout;
        }
    }

    // Constructor for the adapter, initializing the emergency contacts list.
    public SettingsAdapter(ArrayList<EmergencyContact> emergency_contacts) {
        this.emergency_contacts = emergency_contacts;
    }

    // Creates a new ViewHolder by inflating the item layout.
    @NonNull
    @Override
    public SettingsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emerrgency_contact_recycler_view, parent, false);
        return new ViewHolder(view);
    }

    // Binds the data from an EmergencyContact object to the views in the ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull SettingsAdapter.ViewHolder holder, int position) {
        // Set the contact's name, phone, email, and relationship.
        holder.getName().setText(emergency_contacts.get(position).getName());
        holder.getPhone_no().setText(emergency_contacts.get(position).getPhone_no());
        holder.getEmail().setText(emergency_contacts.get(position).getEmail());
        holder.getRelationship().setText(emergency_contacts.get(position).getRelationship());

        // Set an OnClickListener on the main layout to toggle visibility of extra details.
        holder.getRecycler_layout().setOnClickListener(v -> {
            if(holder.getPhone_no_layout().getVisibility() == View.GONE){
                // If hidden, show the extra layouts.
                holder.getPhone_no_layout().setVisibility(View.VISIBLE);
                holder.getEmail_layout().setVisibility(View.VISIBLE);
                holder.getRelationship_layout().setVisibility(View.VISIBLE);
            }
            else{
                // Otherwise, hide the extra layouts.
                holder.getPhone_no_layout().setVisibility(View.GONE);
                holder.getEmail_layout().setVisibility(View.GONE);
                holder.getRelationship_layout().setVisibility(View.GONE);
            }
        });
    }

    // Returns the total number of emergency contacts in the list.
    @Override
    public int getItemCount() {
        return emergency_contacts.size();
    }
}