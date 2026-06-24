package com.kztutorial.termuxiwx.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kztutorial.termuxiwx.R;
import com.kztutorial.termuxiwx.models.Package;

import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

    public interface OnPackageClickListener {
        void onPackageClick(Package pkg);
    }

    private final List<Package> packages;
    private final OnPackageClickListener listener;

    public PackageAdapter(List<Package> packages, OnPackageClickListener listener) {
        this.packages = packages;
        this.listener = listener;
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
        holder.pkgStatus.setText(pkg.isInstalled() ? "✓ Installed" : "");
        holder.pkgStatus.setVisibility(pkg.isInstalled() ? View.VISIBLE : View.GONE);
        holder.card.setOnClickListener(v -> listener.onPackageClick(pkg));
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView pkgName, pkgVersion, pkgStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            pkgName = itemView.findViewById(R.id.pkg_name);
            pkgVersion = itemView.findViewById(R.id.pkg_version);
            pkgStatus = itemView.findViewById(R.id.pkg_status);
        }
    }
}
