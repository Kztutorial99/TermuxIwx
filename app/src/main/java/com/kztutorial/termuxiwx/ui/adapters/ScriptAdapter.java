package com.kztutorial.termuxiwx.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.kztutorial.termuxiwx.R;
import com.kztutorial.termuxiwx.models.ScriptItem;

import java.util.ArrayList;
import java.util.List;

public class ScriptAdapter extends RecyclerView.Adapter<ScriptAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ScriptItem item);
    }

    private List<ScriptItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public ScriptAdapter(List<ScriptItem> initialItems, OnItemClickListener listener) {
        this.items = new ArrayList<>(initialItems);
        this.listener = listener;
    }

    public void updateList(List<ScriptItem> newList) {
        final List<ScriptItem> oldList = items;
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return newList.size(); }

            @Override
            public boolean areItemsTheSame(int op, int np) {
                return oldList.get(op).getName().equals(newList.get(np).getName());
            }

            @Override
            public boolean areContentsTheSame(int op, int np) {
                ScriptItem o = oldList.get(op);
                ScriptItem n = newList.get(np);
                return o.getName().equals(n.getName())
                    && o.getCategory().equals(n.getCategory());
            }
        });
        items = new ArrayList<>(newList);
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_script, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScriptItem item = items.get(position);
        holder.name.setText(item.getName());
        holder.description.setText(item.getDescription());
        holder.category.setText(item.getCategory());

        int catColor;
        switch (item.getCategory()) {
            case "Security": catColor = R.color.error_color;    break;
            case "Dev":      catColor = R.color.colorPrimary;   break;
            case "Network":  catColor = R.color.warning_color;  break;
            case "Shell":    catColor = R.color.colorAccent;    break;
            case "Database": catColor = R.color.color_database; break;
            case "Editor":   catColor = R.color.color_editor;   break;
            case "Media":    catColor = R.color.color_media;    break;
            case "Termux":   catColor = R.color.success_color;  break;
            default:         catColor = R.color.text_secondary; break;
        }
        holder.category.setTextColor(holder.category.getContext().getColor(catColor));
        holder.card.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView name, description, category;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            name = itemView.findViewById(R.id.script_name);
            description = itemView.findViewById(R.id.script_desc);
            category = itemView.findViewById(R.id.script_category);
        }
    }
}
