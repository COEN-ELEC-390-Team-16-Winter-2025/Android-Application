package com.drinkwise.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder>{

    private final ArrayList<EmergencyContact> emergency_contacts;
    private OnContactActionListener actionListener;

    public interface OnContactActionListener {
        void onEditContact(EmergencyContact contact, int position);
        void onDeleteContact(EmergencyContact contact, int position);
    }

    public void setOnContactActionListener(OnContactActionListener listener) {
        this.actionListener = listener;
    }

    public SettingsAdapter(ArrayList<EmergencyContact> emergency_contacts) {
        this.emergency_contacts = emergency_contacts;
    }

    @NonNull
    @Override
    public SettingsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emerrgency_contact_recycler_view, parent, false);
        return new ViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return emergency_contacts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone_no, email, relationship;
        Button editButton, deleteButton;
        View recycler_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_recycler);
            phone_no = itemView.findViewById(R.id.phone_no_recycler);
            email = itemView.findViewById(R.id.email_recycler);
            relationship = itemView.findViewById(R.id.relationship_recycler);
            recycler_layout = itemView.findViewById(R.id.recycler_layout);
            editButton = itemView.findViewById(R.id.edit_contact_button);
            deleteButton = itemView.findViewById(R.id.delete_contact_button);
        }
    }
}