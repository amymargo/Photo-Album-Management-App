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
import com.example.android.R;
import com.example.android.SearchPhotoViewerActivity;

import java.util.ArrayList;
import java.util.List;

public class SearchPhotoAdapter extends RecyclerView.Adapter<SearchPhotoAdapter.SearchPhotoViewHolder> {

    private List<Photo> photos;
    private Context context;

    public SearchPhotoAdapter(List<Photo> photos, Context context) {
        this.photos = photos;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new SearchPhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchPhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);
        Uri uri = photo.getPhotoUri();
        if (uri != null) {
            holder.imageView.setImageURI(uri);
        }

        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SearchPhotoViewerActivity.class);
            intent.putExtra("searchResults", new ArrayList<>(photos)); // requires Photo to implement Serializable
            intent.putExtra("photoIndex", holder.getAdapterPosition());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class SearchPhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SearchPhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photoImageView);
        }
    }
}