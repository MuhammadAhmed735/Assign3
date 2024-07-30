package com.example.chatify;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messages;
    private  int position;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            SentMessageViewHolder sentViewHolder = (SentMessageViewHolder) holder;
            sentViewHolder.messageText.setText(message.getMessageText());
            sentViewHolder.messageTime.setText(message.getTimeString());
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ReceivedMessageViewHolder receivedViewHolder = (ReceivedMessageViewHolder) holder;
            receivedViewHolder.messageText.setText(message.getMessageText());
            receivedViewHolder.messageTime.setText(message.getTimeString());
        }
        holder.itemView.setOnLongClickListener(v -> {

            return false;
        });
    }
    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isSentByUser ? 0 : 1;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    public class SentMessageViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        EditText messageText;
        TextView messageTime;
        public SentMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sentMessage_editText);
            messageTime = itemView.findViewById(R.id.sent_time);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            position = getAdapterPosition();
            MenuInflater inflater = ((Activity) v.getContext()).getMenuInflater();
            inflater.inflate(R.menu.chat_message_context_menu, menu);
        }
    }
    public  class ReceivedMessageViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        EditText messageText;
        TextView messageTime;

        public ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.receivedMessage_EditText);
            messageTime = itemView.findViewById(R.id.received_time);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            position = getAdapterPosition();
            MenuInflater inflater = ((Activity) v.getContext()).getMenuInflater();
            inflater.inflate(R.menu.chat_message_context_menu, menu);
        }
    }
}

