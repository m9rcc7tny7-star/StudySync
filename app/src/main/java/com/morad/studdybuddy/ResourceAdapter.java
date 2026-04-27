package com.morad.studdybuddy;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(ResourceItem resource);
    }

    private List<ResourceItem> resourceList;
    private String currentUserName;
    private OnDeleteClickListener deleteClickListener;

    public ResourceAdapter(List<ResourceItem> resourceList, String currentUserName, OnDeleteClickListener deleteClickListener) {
        this.resourceList = resourceList;
        this.currentUserName = currentUserName;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        ResourceItem resource = resourceList.get(position);

        holder.title.setText(resource.getTitle());

        String details = resource.getDescription() + "\nUploaded by: " + resource.getUploadedBy();
        holder.details.setText(details);

        holder.itemView.setOnClickListener(v -> {
            String link = resource.getLink();

            if (link == null || link.trim().isEmpty()) {
                Toast.makeText(v.getContext(), "No link available", Toast.LENGTH_SHORT).show();
                return;
            }

            link = link.trim();

            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                link = "https://" + link;
            }

            if (!Patterns.WEB_URL.matcher(link).matches()) {
                Toast.makeText(v.getContext(), "Invalid link", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Uri uri = Uri.parse(link);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                v.getContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(v.getContext(), "No app can open this link", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (resource.getUploadedBy() != null && resource.getUploadedBy().equals(currentUserName)) {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                popupMenu.getMenu().add("Delete");

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals("Delete")) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Delete Resource")
                                .setMessage("Are you sure you want to delete this resource?")
                                .setPositiveButton("Delete", (dialog, which) -> deleteClickListener.onDelete(resource))
                                .setNegativeButton("Cancel", null)
                                .show();
                        return true;
                    }
                    return false;
                });

                popupMenu.show();
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return resourceList.size();
    }

    static class ResourceViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView details;

        public ResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            details = itemView.findViewById(android.R.id.text2);
        }
    }
}
