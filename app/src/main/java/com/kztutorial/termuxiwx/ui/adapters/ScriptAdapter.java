package com.kztutorial.termuxiwx.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kztutorial.termuxiwx.R;
import com.kztutorial.termuxiwx.models.ScriptItem;

import java.util.List;

public class ScriptAdapter extends RecyclerView.Adapter<ScriptAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ScriptItem item);
    }

    private final List<ScriptItem> items;
    private final OnItemClickListener listener;

    public ScriptAdapter(List<ScriptItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
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
        holder.name.setText(item.name);
        holder.description.setText(item.description);
        holder.category.setText(item.category);

        int catColor;
        switch (item.category) {
            case "Security": catColor = R.color.error_color; break;
            case "Dev": catColor = R.color.colorPrimary; break;
            case "Network": catColor = R.color.warning_color; break;
            case "Shell": catColor = R.color.colorAccent; break;
            default: catColor = R.color.text_secondary; break;
        }
        holder.category.setTextColor(holder.category.getContext().getColor(catColor));
        holder.card.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
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
