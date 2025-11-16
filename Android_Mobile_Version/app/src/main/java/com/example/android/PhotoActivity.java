package com.example.android;

import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.utils.AlbumStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoActivity extends AppCompatActivity {

    private ImageView photoView;
    private TextView captionView;
    private Spinner tagTypeSpinner;
    private EditText tagValueEditText;
    private Button addTagButton, prevButton, nextButton;
    private Button deletePhotoButton;
    private LinearLayout personTagContainer;
    private LinearLayout locationTagContainer;    

    private Album album;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // back button

        // Get album index and photo index from intent
        int albumIndex = getIntent().getIntExtra("albumIndex", -1);
        currentIndex = getIntent().getIntExtra("photoIndex", -1);

        if (albumIndex == -1 || currentIndex == -1) {
            finish(); // invalid input
            return;
        }

        album = MainActivity.getAlbums().get(albumIndex);

        // Hook up UI
        photoView = findViewById(R.id.fullPhotoImageView);
        captionView = findViewById(R.id.captionTextView);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        deletePhotoButton = findViewById(R.id.deletePhotoButton);
        personTagContainer = findViewById(R.id.personTagContainer);
        locationTagContainer = findViewById(R.id.locationTagContainer);



        updatePhotoView();

        prevButton.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                updatePhotoView();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentIndex < album.getPhotos().size() - 1) {
                currentIndex++;
                updatePhotoView();
            }
        });     

        deletePhotoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Photo")
                    .setMessage("Are you sure you want to delete this photo from the album?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        album.getPhotos().remove(currentIndex);
                        AlbumStorage.saveAlbums(this, MainActivity.getAlbums());

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("photoDeleted", true);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        Button moveButton = findViewById(R.id.btnMovePhoto);
        moveButton.setOnClickListener(v -> showMoveDialog());        
    }

    private void updatePhotoView() {
        Photo photo = album.getPhotos().get(currentIndex);
        photoView.setImageURI(photo.getPhotoUri());
        captionView.setText(photo.getCaption());
    
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
    
            // Add "+" chip
            View addChip = getLayoutInflater().inflate(R.layout.tag_chip, null);
            TextView tagText = addChip.findViewById(R.id.tagText);
            ImageButton deleteBtn = addChip.findViewById(R.id.deleteTagBtn);
            tagText.setText("+");
            deleteBtn.setVisibility(View.GONE);
    
            addChip.setOnClickListener(v -> {
                container.removeView(addChip);
    
                View inputChip = getLayoutInflater().inflate(R.layout.tag_chip_input, null);
                EditText inputField = inputChip.findViewById(R.id.tagInputField);
                ImageButton confirmBtn = inputChip.findViewById(R.id.confirmTagBtn);
    
                inputField.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(inputField, InputMethodManager.SHOW_IMPLICIT);
    
                confirmBtn.setOnClickListener(confirmView -> {
                    String newValue = inputField.getText().toString().trim();
                    if (!newValue.isEmpty()) {
                        photo.getTags().add(new Tag(type, newValue));
                        AlbumStorage.saveAlbums(this, MainActivity.getAlbums());
                        updatePhotoView(); // refresh tags
                    }
                });
    
                container.addView(inputChip, 0);
            });
    
            container.addView(addChip);
    
            List<String> values = tagMap.getOrDefault(type, new ArrayList<>());
            for (String value : values) {
                View chip = getLayoutInflater().inflate(R.layout.tag_chip, null);
                TextView chipText = chip.findViewById(R.id.tagText);
                ImageButton deleteBtnReal = chip.findViewById(R.id.deleteTagBtn);
    
                chipText.setText(value);
                Tag tag = new Tag(type, value);
    
                deleteBtnReal.setOnClickListener(v -> {
                    photo.getTags().remove(tag);
                    AlbumStorage.saveAlbums(this, MainActivity.getAlbums());
                    updatePhotoView();
                });
    
                container.addView(chip, 1);
                View parent = (View) container.getParent();
            }
        }
    }    

    private void showError(String message) {
        new android.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showMoveDialog() {
        List<Album> albums = MainActivity.getAlbums();
        List<String> albumNames = new ArrayList<>();
    
        for (Album a : albums) {
            if (!a.equals(album)) {
                albumNames.add(a.getName());
            }
        }
    
        if (albumNames.isEmpty()) {
            showError("No other albums to move the photo to.");
            return;
        }
    
        String[] options = albumNames.toArray(new String[0]);
    
        new AlertDialog.Builder(this)
            .setTitle("Move photo to:")
            .setItems(options, (dialog, which) -> {
                String targetAlbumName = options[which];
                Album targetAlbum = null;
    
                for (Album a : albums) {
                    if (a.getName().equals(targetAlbumName)) {
                        targetAlbum = a;
                        break;
                    }
                }
    
                if (targetAlbum != null) {
                    Photo photo = album.getPhotos().get(currentIndex);
    
                    if (targetAlbum.addPhoto(photo)) {
                        album.removePhoto(photo);
                        AlbumStorage.saveAlbums(this, albums);
                        Toast.makeText(this, "Photo moved to " + targetAlbumName, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        showError("Photo already exists in " + targetAlbumName);
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }    

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // go back to AlbumActivity
        return true;
    }
}
