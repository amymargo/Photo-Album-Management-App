package com.example.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchPhotoViewerActivity extends AppCompatActivity {

    private ImageView photoImageView;
    private TextView filenameTextView;
    private Button prevButton, nextButton, showInAlbumButton;

    private ArrayList<Photo> searchResults;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_photo_viewer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        photoImageView = findViewById(R.id.searchPhotoImageView);
        filenameTextView = findViewById(R.id.filenameTextView);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        showInAlbumButton = findViewById(R.id.showInAlbumButton);

        searchResults = (ArrayList<Photo>) getIntent().getSerializableExtra("searchResults");
        currentIndex = getIntent().getIntExtra("photoIndex", 0);

        updateView();

        prevButton.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                updateView();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentIndex < searchResults.size() - 1) {
                currentIndex++;
                updateView();
            }
        });

        showInAlbumButton.setOnClickListener(v -> {
            Photo currentPhoto = searchResults.get(currentIndex);
            for (int i = 0; i < MainActivity.getAlbums().size(); i++) {
                Album album = MainActivity.getAlbums().get(i);
                int index = album.getPhotos().indexOf(currentPhoto);
                if (index != -1) {
                    Intent mainIntent = new Intent(this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    Intent albumIntent = new Intent(this, AlbumActivity.class);
                    albumIntent.putExtra("albumIndex", i);

                    Intent photoIntent = new Intent(this, PhotoActivity.class);
                    photoIntent.putExtra("albumIndex", i);
                    photoIntent.putExtra("photoIndex", index);

                    startActivities(new Intent[]{mainIntent, albumIntent, photoIntent});
                    return;
                }
            }
        });
    }

    private void updateView() {
        Photo photo = searchResults.get(currentIndex);
        photoImageView.setImageURI(photo.getPhotoUri());
        filenameTextView.setText(photo.getCaption());
    
        LinearLayout personTagContainer = findViewById(R.id.personTagContainer);
        LinearLayout locationTagContainer = findViewById(R.id.locationTagContainer);
    
        personTagContainer.removeAllViews();
        locationTagContainer.removeAllViews();
    
        Map<String, List<String>> tagMap = new HashMap<>();
        for (Tag tag : photo.getTags()) {
            tagMap.putIfAbsent(tag.getType(), new ArrayList<>());
            tagMap.get(tag.getType()).add(tag.getValue());
        }
    
        String[] types = {"person", "location"};
        for (String type : types) {
            LinearLayout container = type.equals("person") ? personTagContainer : locationTagContainer;
            List<String> values = tagMap.getOrDefault(type, new ArrayList<>());
    
            for (String value : values) {
                View chip = getLayoutInflater().inflate(R.layout.tag_chip, null);
                TextView chipText = chip.findViewById(R.id.tagText);
                ImageButton deleteBtn = chip.findViewById(R.id.deleteTagBtn);
    
                chipText.setText(value);
                deleteBtn.setVisibility(View.GONE); // no delete in viewer
    
                container.addView(chip);
            }
        }
    }    

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
