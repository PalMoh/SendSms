package com.example.sendsms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.sendsms.Adapter.MyAdapter;
import com.example.sendsms.Model.Contacts;

import java.util.List;

public class ContactActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<Contacts> contactsList;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);


    }
}
