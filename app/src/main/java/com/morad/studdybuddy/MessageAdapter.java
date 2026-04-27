package com.morad.studdybuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<Message> messageList;
    private final String currentUserId;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        }
        return VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        String timeText = formatTimestamp(message.getTimestamp());
        String senderText;

        if (message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            senderText = "You • " + timeText;
        } else {
            senderText = message.getSenderName() + " • " + timeText;
        }

        holder.tvSender.setText(senderText);
        holder.tvMessage.setText(message.getText());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private String formatTimestamp(long timestamp) {
        Calendar now = Calendar.getInstance();
        Calendar msg = Calendar.getInstance();
        msg.setTimeInMillis(timestamp);

        boolean sameDay =
                now.get(Calendar.YEAR) == msg.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == msg.get(Calendar.DAY_OF_YEAR);

        if (sameDay) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(timestamp));
        } else {
            return new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                    .format(new Date(timestamp));
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}
