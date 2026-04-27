package com.morad.studdybuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudyGroupAdapter extends RecyclerView.Adapter<StudyGroupAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(String groupId, StudyGroup group);
    }

    private List<StudyGroup> groupList;
    private List<String> groupIds;
    private OnGroupClickListener listener;

    public StudyGroupAdapter(List<StudyGroup> groupList, List<String> groupIds, OnGroupClickListener listener) {
        this.groupList = groupList;
        this.groupIds = groupIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);

        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {

        StudyGroup group = groupList.get(position);
        String groupId = groupIds.get(position);

        holder.groupName.setText(group.getName());

        holder.itemView.setOnClickListener(v -> listener.onGroupClick(groupId, group));
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {

        TextView groupName;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(android.R.id.text1);
        }
    }
}
