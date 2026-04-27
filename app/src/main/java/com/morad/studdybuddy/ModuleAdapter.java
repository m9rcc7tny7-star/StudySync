package com.morad.studdybuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder> {

    public interface OnModuleClickListener {
        void onModuleClick(Module module);
    }

    private final List<Module> moduleList;
    private final OnModuleClickListener listener;

    public ModuleAdapter(List<Module> moduleList, OnModuleClickListener listener) {
        this.moduleList = moduleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module_card, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        Module module = moduleList.get(position);

        holder.tvModuleCode.setText(module.getModuleCode());
        holder.tvModuleName.setText(module.getModuleName());

        holder.itemView.setOnClickListener(v -> listener.onModuleClick(module));
    }

    @Override
    public int getItemCount() {
        return moduleList.size();
    }

    static class ModuleViewHolder extends RecyclerView.ViewHolder {

        TextView tvModuleCode, tvModuleName, tvModuleHint;

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvModuleCode = itemView.findViewById(R.id.tvModuleCode);
            tvModuleName = itemView.findViewById(R.id.tvModuleName);
            tvModuleHint = itemView.findViewById(R.id.tvModuleHint);
        }
    }
}