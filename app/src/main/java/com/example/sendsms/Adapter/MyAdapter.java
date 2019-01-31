package com.example.sendsms.Adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sendsms.Model.Contacts;
import com.example.sendsms.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Contacts> contactsList;

    public MyAdapter(List<Contacts> contactsList) {
        this.contactsList = contactsList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_contacts,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Contacts contacts = this.contactsList.get(position);
        MyViewHolder myViewHolder = (MyViewHolder) holder;

        myViewHolder.tvName.setText(contacts.getContactName());
        myViewHolder.tvNumber.setText(contacts.getContactNumber());


    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName, tvNumber;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvNumber = itemView.findViewById(R.id.tvNumber);
        }
    }
}