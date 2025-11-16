package com.example.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.Photo;
import com.example.android.PhotoActivity;
import com.example.android.R;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<Photo> photos;
    private Context context;
    private int albumIndex; // ADD THIS

    public PhotoAdapter(List<Photo> photos, Context context, int albumIndex) {
        this.photos = photos;
        this.context = context;
        this.albumIndex = albumIndex;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);
        Uri uri = photo.getPhotoUri();

        // Load image into ImageView
        if (uri != null){
            holder.imageView.setImageURI(uri);
        }

        holder.imageView.setImageURI(photo.getPhotoUri());

        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PhotoActivity.class);
            intent.putExtra("albumIndex", albumIndex);
            intent.putExtra("photoIndex", position);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photoImageView);
        }
    }
}