package com.morad.studdybuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudySessionAdapter extends RecyclerView.Adapter<StudySessionAdapter.SessionViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(String sessionId, StudySession session);
    }

    private List<StudySession> sessionList;
    private List<String> sessionIds;
    private OnSessionClickListener listener;

    public StudySessionAdapter(List<StudySession> sessionList, List<String> sessionIds, OnSessionClickListener listener) {
        this.sessionList = sessionList;
        this.sessionIds = sessionIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);

        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        StudySession session = sessionList.get(position);
        String sessionId = sessionIds.get(position);

        holder.topic.setText(session.getTopic());

        String secondLine;
        if ("Online".equals(session.getMode())) {
            secondLine = session.getDate() + " • " +
                    session.getTime() + " • Online";
        } else {
            secondLine = session.getDate() + " • " +
                    session.getTime() + " • " + session.getLocation();
        }

        holder.details.setText(secondLine);

        holder.itemView.setOnClickListener(v -> listener.onSessionClick(sessionId, session));
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {

        TextView topic;
        TextView details;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);

            topic = itemView.findViewById(android.R.id.text1);
            details = itemView.findViewById(android.R.id.text2);
        }
    }
}
