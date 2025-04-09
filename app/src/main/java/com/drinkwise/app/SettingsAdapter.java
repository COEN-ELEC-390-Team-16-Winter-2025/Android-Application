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

// Adapter for displaying EmergencyContact objects in a RecyclerView
public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder>{

    private ArrayList<EmergencyContact> emergency_contacts;

    // class that holds the view references for each item in the list
    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name, phone_no, email, relationship;
        ImageView profile_picture;
        LinearLayout recycler_layout, phone_no_layout, email_layout, relationship_layout;

        // constructor
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
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

    // constructor for the adapter
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

    // connects the data from an EmergencyContact object to the views in the ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull SettingsAdapter.ViewHolder holder, int position) {
        EmergencyContact contact = emergency_contacts.get(position);
        holder.name.setText(contact.getName());
        holder.phone_no.setText(contact.getPhone_no());
        holder.email.setText(contact.getEmail());
        holder.relationship.setText(contact.getRelationship());

        // Set up Edit button listener
        holder.editButton.setOnClickListener(v -> {
            if(actionListener != null){
                actionListener.onEditContact(contact, position);
            }
        });
        // Set up Delete button listener
        holder.deleteButton.setOnClickListener(v -> {
            if(actionListener != null){
                actionListener.onDeleteContact(contact, position);
            }
        });

        holder.recycler_layout.setOnClickListener(v -> {
            if(holder.phone_no.getVisibility() == View.GONE){
                holder.phone_no.setVisibility(View.VISIBLE);
                holder.email.setVisibility(View.VISIBLE);
                holder.relationship.setVisibility(View.VISIBLE);
            } else {
                holder.phone_no.setVisibility(View.GONE);
                holder.email.setVisibility(View.GONE);
                holder.relationship.setVisibility(View.GONE);
            }
        });
    }

    // returns the total number of emergency contacts in the list
    @Override
    public int getItemCount() {
        return emergency_contacts.size();
    }
}