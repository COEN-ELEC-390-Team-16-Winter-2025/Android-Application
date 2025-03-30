package com.drinkwise.app;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder>{

    private ArrayList<EmergencyContact> emergency_contacts = new ArrayList<EmergencyContact>();
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

        holder.getName().setText(emergency_contacts.get(position).getName());
        holder.getPhone_no().setText(emergency_contacts.get(position).getPhone_no());
        holder.getEmail().setText(emergency_contacts.get(position).getEmail());
        holder.getRelationship().setText(emergency_contacts.get(position).getRelationship());

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

    @Override
    public int getItemCount() {
        return emergency_contacts.size();
    }
}
