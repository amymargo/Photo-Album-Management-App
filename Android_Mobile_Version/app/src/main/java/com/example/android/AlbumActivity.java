package com.example.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.adapters.PhotoAdapter;
import com.example.android.utils.AlbumStorage;

import android.view.Menu;
import android.view.MenuItem;

public class AlbumActivity extends AppCompatActivity {

    private Album album;
    private PhotoAdapter adapter;

    private final ActivityResultLauncher<Intent> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri photoUri = result.getData().getData();
                    if (photoUri != null) {
                        getContentResolver().takePersistableUriPermission(
                                photoUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        Photo photo = new Photo(photoUri, this);
                        album.getPhotos().add(photo);
                        adapter.notifyItemInserted(album.getPhotos().size() - 1);
                        AlbumStorage.saveAlbums(this, MainActivity.getAlbums());
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int albumIndex = getIntent().getIntExtra("albumIndex", -1);
        album = MainActivity.getAlbums().get(albumIndex);

        RecyclerView recyclerView = findViewById(R.id.photoRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columns
        adapter = new PhotoAdapter(album.getPhotos(), this, albumIndex);
        recyclerView.setAdapter(adapter);

        Button addPhotoButton = findViewById(R.id.addPhotoButton);
        addPhotoButton.setOnClickListener(v -> openPhotoPicker());
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        photoPickerLauncher.launch(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged(); // Refresh thumbnail grid
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Go back to previous screen
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);  // use your existing menu_main.xml
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}