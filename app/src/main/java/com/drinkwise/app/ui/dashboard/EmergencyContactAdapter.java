package com.drinkwise.app.ui.dashboard;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drinkwise.app.EmergencyContact;
import com.drinkwise.app.R;

import java.util.ArrayList;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder> {

    // This provides a way for the adapter to notify us when a contact is clicked so we can respond accordingly
    public interface OnContactClickListener {
        void onContactClick(EmergencyContact contact);
    }

    @FunctionalInterface
    public interface OnEditContactListener {
        void onEditContact(EmergencyContact contact);
    }

    @FunctionalInterface
    public interface OnDeleteContactListener {
        void onDeleteContact(EmergencyContact contact);
    }

    // The list of emergency contacts to be displayed in the RecyclerView
    ArrayList<EmergencyContact> emergencyContacts;
    OnContactClickListener listener;
    OnEditContactListener editListener;
    OnDeleteContactListener deleteListener;

    // This sets up the adapter with a list of contacts and a function to handle clicks
    public EmergencyContactAdapter(ArrayList<EmergencyContact> emergencyContacts,
                                   OnContactClickListener listener) {
        this.emergencyContacts = emergencyContacts;
        this.listener = listener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    // This is called when RecyclerView needs a new ViewHolder of the given type.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // this creates the view for one emergency contact
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emerrgency_contact_recycler_view, parent, false);
        return new ViewHolder(view);
    }

    // method that connects the data to the ViewHolder for the item at the given position.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyContact contact = emergencyContacts.get(position);
        holder.getName().setText(contact.getName());
        holder.getPhone_no().setText(contact.getPhone_no());
        holder.getEmail().setText(contact.getEmail());
        holder.getRelationship().setText(contact.getRelationship());

        //When it is clicked, the adapter will notify the listener
        holder.getRecycler_layout().setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(emergencyContacts.get(position));
            }
        });

        holder.editButton.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditContact(contact);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteContact(contact);
            }
        });
    }

    // Returns the total number of items in the data set held by the adapter.
    @Override
    public int getItemCount() {
        return emergencyContacts.size();
    }

    // The ViewHolder class holds the views for each individual item.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone_no, email, relationship;
        ImageView profile_picture;
        LinearLayout recycler_layout, phone_no_layout, email_layout, relationship_layout;
        ImageButton editButton, deleteButton;

        // Constructor that initializes the view references.
        @SuppressLint("WrongViewCast")
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

            editButton = itemView.findViewById(R.id.edit_contact_button);
            deleteButton = itemView.findViewById(R.id.delete_contact_button);
        }

        // Getter methods
        public TextView getName() {return name;}
        public TextView getPhone_no() {return phone_no;}
        public TextView getEmail() {return email;}
        public TextView getRelationship() {return relationship;}
        public ImageView getProfile_picture() {return profile_picture;}
        public LinearLayout getPhone_no_layout() {return phone_no_layout;}
        public LinearLayout getEmail_layout() {return email_layout;}
        public LinearLayout getRelationship_layout() {return relationship_layout;}
        public LinearLayout getRecycler_layout() {return recycler_layout;}
    }
}