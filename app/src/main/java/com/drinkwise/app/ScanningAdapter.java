package com.drinkwise.app;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class ScanningAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<BACEntry> bacEntries;

    private LayoutInflater inflater;

    public ScanningAdapter(Context context, ArrayList<BACEntry> bacEntries) {
        this.context = context;
        this.bacEntries = bacEntries;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return bacEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.bac_list_view, null);

        TextView bac_value = convertView.findViewById(R.id.bac_value);
        TextView bac_status_holder = convertView.findViewById(R.id.bac_status_holder);
        TextView bac_status = convertView.findViewById(R.id.bac_status);
        TextView bac_date = convertView.findViewById(R.id.bac_date);
        TextView bac_time = convertView.findViewById(R.id.bac_time);

        double bac = bacEntries.get(position).getBac();
        bac_value.setText("BAC Value: " + String.format(Locale.US, "%.3f", bac));
        bac_status_holder.setText("Status: ");
        bac_status.setText(bacEntries.get(position).getStatus());
        bac_date.setText("Date: "+bacEntries.get(position).getDate());
        bac_time.setText("Time: "+bacEntries.get(position).getTime());



        switch(bacEntries.get(position).getStatus().trim()){
            case "Safe":
                bac_status.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                break;

            case "Caution":
                bac_status.setBackgroundColor(ContextCompat.getColor(context, R.color.button_orange));
                break;
            case "Over Limit":
                bac_status.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                break;
            default:
                break;
        }


        return convertView;
    }
}
