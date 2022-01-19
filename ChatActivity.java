package com.example.clone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.clone.Adapter.ChatAdapter;
import com.example.clone.Models.MessageModel;
import com.example.clone.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //hide extra toolbar :always default
        getSupportActionBar().hide();

        //initialize firebase
        database = FirebaseDatabase.getInstance();
        auth =  FirebaseAuth.getInstance();


        final String senderId = auth.getUid();
        String receiveId = getIntent().getStringExtra("userId");
        String  userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");//use same text format in the user model


        // setting name and profile picture
        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profimage);

        //initializing back arrow

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //moves back to main activity
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);

                startActivity(intent);
            }
        });

        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModels, this, receiveId);

        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);


        //Identifying which user is sender and which is receiver
        final String senderRoom = senderId + receiveId;
        final String receiveRoom = receiveId + senderId;

        //set onclick listener to send button
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*1: Extract message from the particular text  field
                     store it as a variable */

                String message = binding.entMess.getText().toString();
                final MessageModel model = new MessageModel(senderId,message);
                model.setTimestamp(new Date().getTime());
                binding.entMess.setText(""); //set to null

                /* 2: store in the database when it clicked
                      create 'chat' as a child to the 'user'

                      -when message is sent it need to be stored on oth sender and receiver side hence the 2 sections
                */

               database.getReference().child("chats")
                       .child(senderRoom)
                       .push() //set message
                       .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(@NonNull Void unused) {
                       database.getReference().child("chats")
                               .child(receiveRoom)
                               .push()
                               .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(@NonNull Void unused) {

                           }
                       });
                   }
               });


            }
        });


    }


}