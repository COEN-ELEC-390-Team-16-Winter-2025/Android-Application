package com.drinkwise.app.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drinkwise.app.EmergencyContact;
import com.drinkwise.app.R;

import java.util.ArrayList;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder> {

    // Callback interface for handling contact clicks from the adapter.
    public interface OnContactClickListener {
        void onContactClick(EmergencyContact contact);
    }

    // List of emergency contacts to be displayed in the RecyclerView.
    ArrayList<EmergencyContact> emergencyContacts;
    // Listener that will handle click events.
    OnContactClickListener listener;

    // Constructor: initializes the adapter with a list of contacts and a click listener.
    public EmergencyContactAdapter(ArrayList<EmergencyContact> emergencyContacts, OnContactClickListener listener) {
        this.emergencyContacts = emergencyContacts;
        this.listener = listener;
    }

    // Called when RecyclerView needs a new ViewHolder of the given type.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for an individual emergency contact item.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emerrgency_contact_recycler_view, parent, false);
        return new ViewHolder(view);
    }

    // Binds data to the ViewHolder for the item at the given position.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Set the text for name, phone, email, and relationship using data from the emergencyContacts list.
        holder.getName().setText(emergencyContacts.get(position).getName());
        holder.getPhone_no().setText(emergencyContacts.get(position).getPhone_no());
        holder.getEmail().setText(emergencyContacts.get(position).getEmail());
        holder.getRelationship().setText(emergencyContacts.get(position).getRelationship());

        // Set an OnClickListener for the recycler_layout. When clicked, the adapter notifies the listener.
        holder.getRecycler_layout().setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(emergencyContacts.get(position));
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

        // Declare views to display contact information.
        TextView name, phone_no, email, relationship;
        ImageView profile_picture;
        // Layouts that might be toggled for additional details.
        LinearLayout recycler_layout, phone_no_layout, email_layout, relationship_layout;

        // Constructor: initializes the view references.
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by their IDs.
            name = itemView.findViewById(R.id.name_recycler);
            phone_no = itemView.findViewById(R.id.phone_no_recycler);
            email = itemView.findViewById(R.id.email_recycler);
            relationship = itemView.findViewById(R.id.relationship_recycler);

            recycler_layout = itemView.findViewById(R.id.recycler_layout);
            phone_no_layout = itemView.findViewById(R.id.phone_no_layout_recycler);
            email_layout = itemView.findViewById(R.id.email_layout_recycler);
            relationship_layout = itemView.findViewById(R.id.relationship_layout_recycler);
        }

        // Getter methods to access the views from outside this class.
        public TextView getName() { return name; }
        public TextView getPhone_no() { return phone_no; }
        public TextView getEmail() { return email; }
        public TextView getRelationship() { return relationship; }
        public ImageView getProfile_picture() { return profile_picture; }
        public LinearLayout getPhone_no_layout() { return phone_no_layout; }
        public LinearLayout getEmail_layout() { return email_layout; }
        public LinearLayout getRelationship_layout() { return relationship_layout; }
        public LinearLayout getRecycler_layout() { return recycler_layout; }
    }
}