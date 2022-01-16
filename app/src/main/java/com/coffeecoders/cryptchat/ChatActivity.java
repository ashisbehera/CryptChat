package com.coffeecoders.cryptchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.coffeecoders.cryptchat.customAdapters.ChatAdapter;
import com.coffeecoders.cryptchat.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private final static String TAG = "ChatActivity";
    private ActivityChatBinding chatBinding;
    private Intent chatIntent;
    private ChatAdapter chatAdapter;
    private ArrayList<MessageModel> messagesList;
    private FirebaseStorage storage;
    private FirebaseFirestore firebaseFirestore;
    String senderMessage, receiverMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(chatBinding.getRoot());
        chatBinding.chatView.setLayoutManager(new LinearLayoutManager(this));
        chatBinding.chatView.setAdapter(chatAdapter);
        firebaseFirestore = FirebaseFirestore.getInstance();
        messagesList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messagesList);
        chatIntent = getIntent();
        String title = chatIntent.getExtras().getString("name");
        String receiverUid = chatIntent.getExtras().getString("uid");
        String senderUid = FirebaseAuth.getInstance().getUid();
        setTitle(title);
        senderMessage = senderUid + receiverUid;
        receiverMessage = receiverUid + senderUid;
        firebaseFirestore.collection("chats")
                .document(senderMessage)
                .collection("messages")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        List<DocumentSnapshot> list = snapshot.getDocuments();
                        messagesList.clear();
                        for (DocumentSnapshot documentSnapshot : list) {
                            MessageModel messageModel = documentSnapshot.toObject(MessageModel.class);
                            messagesList.add(messageModel);
                        }
                        chatAdapter.notifyDataSetChanged();
                    }
                });


        chatBinding.sendImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String typedMsg = chatBinding.msgEditTxt.getText().toString();
                Date date = new Date();
                MessageModel message = new MessageModel(typedMsg, senderUid, date.getTime());
                chatBinding.msgEditTxt.setText("");
                firebaseFirestore.collection("chats")
                        .document(senderMessage)
                        .collection("messages")
                        .add(message)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                firebaseFirestore.collection("chats")
                                        .document(receiverMessage)
                                        .collection("messages")
                                        .add(message)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {

                                            }
                                        });
                            }
                        });
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}