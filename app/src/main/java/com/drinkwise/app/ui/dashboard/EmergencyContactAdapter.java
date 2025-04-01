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
import com.drinkwise.app.SettingsAdapter;

import java.util.ArrayList;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder> {

    public interface OnContactClickListener {
        void onContactClick(EmergencyContact contact);
    }

    ArrayList<EmergencyContact> emergencyContacts = new ArrayList<>();
    OnContactClickListener listener;

    public EmergencyContactAdapter(ArrayList<EmergencyContact> emergencyContacts, OnContactClickListener listener) {
        this.emergencyContacts = emergencyContacts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emerrgency_contact_recycler_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getName().setText(emergencyContacts.get(position).getName());
        holder.getPhone_no().setText(emergencyContacts.get(position).getPhone_no());
        holder.getEmail().setText(emergencyContacts.get(position).getEmail());
        holder.getRelationship().setText(emergencyContacts.get(position).getRelationship());

        holder.getRecycler_layout().setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(emergencyContacts.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return emergencyContacts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name, phone_no, email, relationship;
        ImageView profile_picture;
        LinearLayout recycler_layout, phone_no_layout, email_layout, relationship_layout;


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
