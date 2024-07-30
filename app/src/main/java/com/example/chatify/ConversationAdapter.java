package com.example.chatify;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.MyViewHolder> {

    private Context context;
    private List<Conversation> conversations;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    public ConversationAdapter(Context context, List<Conversation> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        String otherUserId = conversation.getUser1_id().equals(auth.getCurrentUser().getUid()) ? conversation.getUser2_id() : conversation.getUser1_id();

        // Example of binding data to views
        firestore.collection("users")
                .document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            holder.contactNameTextView.setText(user.getDisplayName());
                            if (conversation.getLastMessage() == null) {
                                holder.recentMessageTextView.setText("Start conversation");
                            } else {
                                holder.recentMessageTextView.setText(conversation.getLastMessage().getMessageText());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to fetch user details
                });

        // Set click listener on item view
        holder.itemView.setOnClickListener(v -> {


             Intent intent = new Intent(context, ChatActivity.class);
             intent.putExtra("conversation_id", conversation.getConversation_id());
            intent.putExtra("receiver_id", otherUserId);
             context.startActivity(intent);
            // or pass the conversation object to an interface method for handling in MainActivity

           /* if (listener != null) {
                listener.onConversationClicked(conversation);
            }

            */
        });


    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    // ViewHolder class
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView contactNameTextView;
        TextView recentMessageTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            contactNameTextView = itemView.findViewById(R.id.contact_name);
            recentMessageTextView = itemView.findViewById(R.id.recent_message);
        }
    }

    // Interface for click listener
    public interface OnConversationClickListener {
        void onConversationClicked(Conversation conversation);
    }

    private OnConversationClickListener listener;

    // Setter for the click listener
    public void setOnConversationClickListener(OnConversationClickListener listener) {
        this.listener = listener;
    }
}
