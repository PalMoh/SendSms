package com.example.sendsms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sendsms.Adapter.MyAdapter;
import com.example.sendsms.Model.Contacts;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private List<Contacts> contactsList = new ArrayList<>();
    private MyAdapter adapter;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    private FloatingActionButton fabSend;
    private Button btnAddContacts;
    private View viewNoContacts;

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser !=null) {
            Log.d(TAG,"The user is not null");
            updateCurrentUser(currentUser);
        }
        else {
            Log.d(TAG,"The user is null ");
            // you can sing in using email and password or google account instead ..
            
            mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        updateCurrentUser(firebaseUser);
                        Toast.makeText(MainActivity.this, "Sing in Anonymously", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.SEND_SMS)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }


        initTheUIViews();
        bindRecyclerView();
        bindBtnSendOnClick();
        bindAddContactsClick();

    }

    private void updateCurrentUser(FirebaseUser firebaseUser){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String userUID = firebaseUser.getUid();
        dbRef = db.getReference(userUID);
        Log.d(TAG, userUID);

        getAllContacts();

    }

    private void bindAddContactsClick() {
        btnAddContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.popup_add_contacts, null);

                dialogBuilder.setView(dialogView);
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
                Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

                final EditText edName = alertDialog.findViewById(R.id.edName);
                final EditText edPhone = alertDialog.findViewById(R.id.edPhone);
                Button btnAddContacts = alertDialog.findViewById(R.id.btnPopupAdd);

                assert btnAddContacts != null;
                btnAddContacts.findViewById(R.id.btnPopupAdd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TextUtils.isEmpty(edName != null ? edName.getText().toString() : null)) {
                            Toast.makeText(MainActivity.this, "Enter name", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        assert edPhone != null;
                        if (TextUtils.isEmpty(edPhone.getText().toString())) {
                            Toast.makeText(MainActivity.this, "Enter Number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String name = edName != null ? edName.getText().toString() : null;
                        String phone = edPhone.getText().toString();
                        addContactToDatabase(name, phone);
                    }
                });
            }
        });


    }

    private void addContactToDatabase(String name, String phone) {

        // check if contacts is already on the list
        for (int i = 0; i < contactsList.size(); i++) {
            if (contactsList.get(i).getContactName().equals(name)) {
                Toast.makeText(this, name + " is already on the list", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Contacts contacts = new Contacts();
        contacts.setContactName(name);
        contacts.setContactNumber(phone);

        dbRef.child("contacts").child(contacts.getContactName()).setValue(contacts).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete())
                    Toast.makeText(MainActivity.this, "Done.. :)", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, "Some thing went wrong..", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initTheUIViews() {
        mRecyclerView = findViewById(R.id.mRecyclerView);
        fabSend = findViewById(R.id.sendMessage);
        btnAddContacts = findViewById(R.id.addContacts);
        viewNoContacts = findViewById(R.id.noContactsView);
    }

    private void bindRecyclerView() {
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new MyAdapter(contactsList);
        mRecyclerView.setAdapter(adapter);
    }

    private void getAllContacts() {

        dbRef.child("contacts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String contactName = snapshot.child("contactName").getValue(String.class);
                    String contactNum = snapshot.child("contactNumber").getValue(String.class);

                    Contacts contacts = new Contacts();
                    contacts.setContactName(contactName);
                    contacts.setContactNumber(contactNum);

                    Log.d(TAG, "contact : " + contactName + "::" + contactNum);
                    contactsList.add(contacts);
                }
                if(contactsList.size() > 0) viewNoContacts.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void bindBtnSendOnClick() {
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contactsList.size() == 0) {
                    Toast.makeText(MainActivity.this, "Add Contacts First", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendMessage();


            }
        });
    }

    private void sendMessage() {
        String message = "enter your message here";
        SmsManager smsManager = SmsManager.getDefault();

        for (int i = 0; i < contactsList.size(); i++) {
            try {
                // Get the default instance of the SmsManager
                smsManager.sendTextMessage(contactsList.get(i).getContactNumber(),
                        null,
                        message,
                        null,
                        null);
            } catch (Exception ex) {
                Toast.makeText(MainActivity.this, "Fail to send",
                        Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        }
        Toast.makeText(MainActivity.this, "Message sent to " + contactsList.size() + " contacts", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }

            }
        }

    }
}
