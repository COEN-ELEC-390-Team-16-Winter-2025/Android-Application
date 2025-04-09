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

        settingsAdapter.setOnContactActionListener(new SettingsAdapter.OnContactActionListener() {
            @Override
            public void onEditContact(EmergencyContact contact, int position) {
                showEditContactDialog(contact, position);  // <-- NEW: Open the edit dialog
            }

            @Override
            public void onDeleteContact(EmergencyContact contact, int position) {
                deleteEmergencyContact(contact, position);  // <-- NEW: Delete the contact
            }
        });

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
        // This sets the contact's name, phone, email, and relationship
        holder.getName().setText(emergency_contacts.get(position).getName());
        holder.getPhone_no().setText(emergency_contacts.get(position).getPhone_no());
        holder.getEmail().setText(emergency_contacts.get(position).getEmail());
        holder.getRelationship().setText(emergency_contacts.get(position).getRelationship());

        // this sets an OnClickListener on the main layout to toggle visibility of extra details.
        holder.getRecycler_layout().setOnClickListener(v -> {
            if(holder.getPhone_no_layout().getVisibility() == View.GONE){
                holder.getPhone_no_layout().setVisibility(View.VISIBLE);
                holder.getEmail_layout().setVisibility(View.VISIBLE);
                holder.getRelationship_layout().setVisibility(View.VISIBLE);
            }
            else{
                holder.getPhone_no_layout().setVisibility(View.GONE);
                holder.getEmail_layout().setVisibility(View.GONE);
                holder.getRelationship_layout().setVisibility(View.GONE);
            }
        });
    }

    // returns the total number of emergency contacts in the list
    @Override
    public int getItemCount() {
        return emergency_contacts.size();
    }
}