package com.sebiai.wallpaperchanger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sebiai.wallpaperchanger.objects.Wallpaper;

public class WallpaperBrowserRecyclerViewAdapter extends RecyclerView.Adapter<WallpaperBrowserRecyclerViewAdapter.ViewHolder> {
    private final Wallpaper[] mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // Constructor
    public WallpaperBrowserRecyclerViewAdapter(Context context, Wallpaper[] data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Set text
        holder.textViewWallpaperName.setText(mData[position].getWallpaperName());

        // Load image
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_wallpaper)
                .error(R.drawable.default_wallpaper);

        Glide.with(holder.imageViewWallpaper.getRootView()).load(mData[position].getWallpaperUri()).apply(options).into(holder.imageViewWallpaper);
    }

    @Override
    public int getItemCount() {
        return mData.length;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageViewWallpaper;
        TextView textViewWallpaperName;
        View viewBottomImageFade;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewWallpaper = itemView.findViewById(R.id.image_view_wallpaper);
            textViewWallpaperName = itemView.findViewById(R.id.textview_wallpaper_name);
            viewBottomImageFade = itemView.findViewById(R.id.view_bottom_image_fade);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public Wallpaper getItem(int id) {
        return mData[id];
    }
}
