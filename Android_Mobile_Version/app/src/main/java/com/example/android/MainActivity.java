package com.example.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.adapters.AlbumAdapter;
import com.example.android.utils.AlbumStorage;

import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AlbumAdapter.OnAlbumActionListener {

    private static List<Album> albums;
    private AlbumAdapter adapter;

    public static List<Album> getAlbums(){
        return albums;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load albums
        albums = AlbumStorage.loadAlbums(this);
        if (albums == null) albums = new ArrayList<>();

        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.albumRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlbumAdapter(albums, this);
        recyclerView.setAdapter(adapter);

        // Handle add album
        Button addAlbumButton = findViewById(R.id.addAlbumButton);
        addAlbumButton.setOnClickListener(v -> showAddAlbumDialog());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddAlbumDialog() {
        final EditText input = new EditText(this);
        input.setHint("Album name");

        new AlertDialog.Builder(this)
                .setTitle("New Album")
                .setMessage("Enter album name:")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();

                    if (name.isEmpty()) {
                        showError("Album name cannot be empty.");
                        return;
                    }

                    for (Album a : albums) {
                        if (a.getName().equalsIgnoreCase(name)) {
                            showError("An album with that name already exists.");
                            return;
                        }
                    }

                    albums.add(new Album(name));
                    adapter.notifyItemInserted(albums.size() - 1);
                    AlbumStorage.saveAlbums(this, albums);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    public void onOpen(Album album) {
        int index = albums.indexOf(album);
        Intent intent = new Intent(this, AlbumActivity.class);
        intent.putExtra("albumIndex", index);
        startActivity(intent);
    }

    @Override
    public void onRename(Album album) {
        final EditText input = new EditText(this);
        input.setText(album.getName());

        new AlertDialog.Builder(this)
                .setTitle("Rename Album")
                .setMessage("Enter new name:")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();

                    if (newName.isEmpty()) {
                        showError("Album name cannot be empty.");
                        return;
                    }

                    for (Album a : albums) {
                        if (a.getName().equalsIgnoreCase(newName) && a != album) {
                            showError("An album with that name already exists.");
                            return;
                        }
                    }

                    album.setName(newName);
                    adapter.notifyDataSetChanged();
                    AlbumStorage.saveAlbums(this, albums);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    public void onDelete(Album album) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Album")
                .setMessage("Are you sure you want to delete " + album.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int index = albums.indexOf(album);
                    albums.remove(album);
                    adapter.notifyItemRemoved(index);
                    AlbumStorage.saveAlbums(this, albums);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    protected void onPause() {
        super.onPause();
        AlbumStorage.saveAlbums(this, albums);
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

}
