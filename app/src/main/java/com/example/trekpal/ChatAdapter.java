package com.example.trekpal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private ArrayList<ChatMessage> chatMessages;
    private String currentUserUniqueCode;  // Unique code for logged-in user

    // Updated constructor to accept currentUserUniqueCode
    public ChatAdapter(ArrayList<ChatMessage> chatMessages, String currentUserUniqueCode) {
        this.chatMessages = chatMessages;
        this.currentUserUniqueCode = currentUserUniqueCode;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        holder.messageTextView.setText(chatMessage.getMessageText());
        holder.timestampTextView.setText(chatMessage.getTimestamp());

        // Check if the message is from the logged-in user
        if (chatMessage.getSenderUniqueCode().equals(currentUserUniqueCode)) {
            // Set background for logged-in user's messages
            holder.messageTextView.setBackgroundResource(R.drawable.logged_in_user_message_bg);
        } else {
            // Set background for other users' messages
            holder.messageTextView.setBackgroundResource(R.drawable.other_user_message_bg);
        }
    }


    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView timestampTextView;

        ChatViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
