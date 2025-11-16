package com.example.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.Album;
import com.example.android.R;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    public interface OnAlbumActionListener {
        void onOpen(Album album);
        void onRename(Album album);
        void onDelete(Album album);
    }

    private List<Album> albums;
    private OnAlbumActionListener listener;

    public AlbumAdapter(List<Album> albums, OnAlbumActionListener listener) {
        this.albums = albums;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.albumNameTextView.setText(album.getName());

        holder.itemView.setOnClickListener(v -> listener.onOpen(album));
        holder.renameButton.setOnClickListener(v -> listener.onRename(album));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(album));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView albumNameTextView;
        Button renameButton;
        Button deleteButton;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumNameTextView = itemView.findViewById(R.id.albumNameTextView);
            renameButton = itemView.findViewById(R.id.renameButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
