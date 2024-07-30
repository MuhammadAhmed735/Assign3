package com.example.chatify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private List<ChatMessage> chatMessages;
    private RecyclerView messagesRecyclerView;
    private EditText chatMessage_editText;
    private FirebaseFirestore firestore;
    private ChatAdapter chatAdapter;
    private String receiverId;
    private String userId;
    private String conversationId;
    private ImageButton sendMessageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverId = getIntent().getStringExtra("receiver_id");
        conversationId = getIntent().getStringExtra("conversation_id");

        firestore = FirebaseFirestore.getInstance();
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        chatMessages = new ArrayList<>();

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        chatMessage_editText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        chatAdapter = new ChatAdapter(chatMessages);
       messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(chatAdapter);

        sendMessageButton.setOnClickListener(this);

        // Load initial messages
        loadMessages();
    }
private void loadMessages() {
    firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("sending_time", Query.Direction.ASCENDING) // Order messages by timestamp (or whatever field represents message time)
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Toast.makeText(getApplicationContext(), "Error fetching messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        Toast.makeText(getApplicationContext(),String.valueOf(queryDocumentSnapshots.size()),Toast.LENGTH_SHORT).show();
                       // chatMessages.clear();
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            ChatMessage chatMessage = dc.getDocument().toObject(ChatMessage.class);
                            switch (dc.getType()) {
                                case ADDED:
                                    if (Objects.equals(chatMessage.getSenderId(), userId)) {
                                        chatMessage.isSentByUser = true;}else {
                                        chatMessage.isSentByUser = false; }
                                    if (!chatMessages.contains(chatMessage)) {
                                        chatMessages.add(chatMessage);
                                    }
                                //    chatMessages.add(chatMessage);
                                    break;
                                case MODIFIED:
                                    // Handle modified messages if needed
                                    break;
                                case REMOVED:
                                    for (int i = 0; i < chatMessages.size(); i++) {
                                        if (chatMessages.get(i).getMessageId().equals(chatMessage.getMessageId())) {
                                            chatMessages.remove(i);
                                            chatAdapter.notifyItemRemoved(i);
                                            break;
                                        }
                                    }
                                    break;
                            }
                        }

                        // Sort chatMessages by timestamp (latest message last)
                        Collections.sort(chatMessages, new Comparator<ChatMessage>() {
                            @Override
                            public int compare(ChatMessage message1, ChatMessage message2) {
                                return message1.getSending_time().compareTo(message2.getSending_time());
                            }
                        });

                        // Notify adapter after sorting
                        chatAdapter.notifyDataSetChanged();
                        messagesRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                    } else {
                        Toast.makeText(getApplicationContext(), "No messages found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
}

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sendMessageButton) {
            String messageToSend = chatMessage_editText.getText().toString().trim();
            if (!messageToSend.isEmpty()) {
                // Create a new message object
                String messageId = userId + "_" + new Date().getTime();
                ChatMessage newMessage = new ChatMessage(messageToSend, userId, receiverId, new Date(),messageId);
                // Store the message in Firestore
                firestore.collection("conversations")
                        .document(conversationId)
                        .collection("messages")
                        .document(messageId)
                        .set(newMessage)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Update the conversation's last message
                                newMessage.isSentByUser=true;
                                updateConversationLastMessage(newMessage);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                            }
                        });



                // Clear the input field
                chatMessage_editText.setText("");

        }
            } else {
                Toast.makeText(getApplicationContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        }


    private void updateConversationLastMessage(ChatMessage message) {
        firestore.collection("conversations")
                .document(conversationId)
                .update("lastMessage", message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Conversation last message updated successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to update conversation", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = -1;
        try {
            position = chatAdapter.getPosition() ;
        } catch (Exception e) {  return super.onContextItemSelected(item); }
        ChatMessage selectedMessage = chatMessages.get(position);
        int itemId = item.getItemId();
        if (itemId == R.id.context_delete) {
            showDeleteConfirmationDialog(selectedMessage, position);
            return true;
        }
        return super.onContextItemSelected(item);
    }
    private void showDeleteConfirmationDialog(ChatMessage message, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(message, position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void deleteMessage(ChatMessage message,int position) {
        firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .document(message.getMessageId()) // Assuming ChatMessage has a method getId() to get the message ID
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (position >= 0 && position < chatMessages.size()) {
                            chatMessages.remove(position); // Remove the message from the list
                            chatAdapter.notifyItemRemoved(position);
                            Toast.makeText(getApplicationContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to delete message", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
