package com.example.opengldemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.ViewHolder> {
    private List<Assets>  mAssetList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View assetView;
        ImageView assetImage;
        TextView assetText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            assetView = itemView;
            assetImage = itemView.findViewById(R.id.asset_image);
            assetText = itemView.findViewById(R.id.asset_name);
        }
    }

    public AssetAdapter(List<Assets> assets) {
        mAssetList = assets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.asset_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.assetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                Assets assets = mAssetList.get(position);
                Toast.makeText(v.getContext(), "正在加载素材"  + assets.getName() + " 请稍候" , Toast.LENGTH_SHORT).show();
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Assets assets = mAssetList.get(position);
        holder.assetText.setText(assets.getName());
        holder.assetImage.setImageResource(assets.getImageId());

    }

    @Override
    public int getItemCount() {
        return mAssetList.size();
    }
}
