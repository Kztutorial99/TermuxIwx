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
import com.kztutorial.termuxiwx.models.Package;

import java.util.ArrayList;
import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

    public interface OnPackageClickListener {
        void onPackageClick(Package pkg);
    }

    private List<Package> packages = new ArrayList<>();
    private final OnPackageClickListener listener;

    public PackageAdapter(OnPackageClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Package> newList) {
        final List<Package> oldList = packages;
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return newList.size(); }

            @Override
            public boolean areItemsTheSame(int op, int np) {
                return oldList.get(op).getName().equals(newList.get(np).getName());
            }

            @Override
            public boolean areContentsTheSame(int op, int np) {
                Package o = oldList.get(op);
                Package n = newList.get(np);
                return o.getName().equals(n.getName())
                    && o.getVersion().equals(n.getVersion())
                    && o.isInstalled() == n.isInstalled();
            }
        });
        packages = new ArrayList<>(newList);
        result.dispatchUpdatesTo(this);
    }

    public int size() {
        return packages.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Package pkg = packages.get(position);
        holder.pkgName.setText(pkg.getName());
        holder.pkgVersion.setText(pkg.getVersion());

        String desc = pkg.getDescription();
        if (desc != null && !desc.isEmpty()) {
            holder.pkgDesc.setText(desc);
            holder.pkgDesc.setVisibility(View.VISIBLE);
        } else {
            holder.pkgDesc.setVisibility(View.GONE);
        }

        if (pkg.isInstalled()) {
            holder.pkgStatus.setText("✓ Installed");
            holder.pkgStatus.setVisibility(View.VISIBLE);
        } else {
            holder.pkgStatus.setVisibility(View.GONE);
        }

        holder.card.setOnClickListener(v -> listener.onPackageClick(pkg));
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView pkgName, pkgVersion, pkgDesc, pkgStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            pkgName = itemView.findViewById(R.id.pkg_name);
            pkgVersion = itemView.findViewById(R.id.pkg_version);
            pkgDesc = itemView.findViewById(R.id.pkg_desc);
            pkgStatus = itemView.findViewById(R.id.pkg_status);
        }
    }
}
