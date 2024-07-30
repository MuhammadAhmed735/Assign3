package com.example.chatify;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.User;

import java.util.List;

public class UsernameAdapter extends RecyclerView.Adapter<UsernameAdapter.ViewHolder> {

    private  Context context;
    private List<User> userList;
    private OnItemClickListener listener;

    // Constructor to initialize the adapter with data and context
    public UsernameAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        Toast.makeText(context, String.valueOf(userList.size()), Toast.LENGTH_SHORT).show();
    }

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    // Method to set item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder class to hold and recycle views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView displayNameTextView;
        TextView usernameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayNameTextView = itemView.findViewById(R.id.searched_name);
            usernameTextView = itemView.findViewById(R.id.searched_username);
        }

        // Method to bind data to views
        public void bind(User user, OnItemClickListener listener) {


            displayNameTextView.setText(user.getDisplayName());
            usernameTextView.setText(user.getUserName());

            // Set click listener on itemView
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(user);
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_username, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
