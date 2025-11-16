package com.example.android;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.android.adapters.SearchPhotoAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {

    private EditText personTagsEditText, locationTagsEditText;
    private Button searchButton;
    private RecyclerView resultsRecyclerView;
    private SearchPhotoAdapter adapter;
    private RadioGroup logicGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        personTagsEditText = findViewById(R.id.personTagsEditText);
        locationTagsEditText = findViewById(R.id.locationTagsEditText);
        searchButton = findViewById(R.id.searchButton);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        logicGroup = findViewById(R.id.logicGroup);

        ArrayAdapter<String> personAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            getAllTagValues("person")
        );
        ((AutoCompleteTextView) personTagsEditText).setAdapter(personAdapter);
        ((AutoCompleteTextView) personTagsEditText).setThreshold(1);

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            getAllTagValues("location")
        );
        ((AutoCompleteTextView) locationTagsEditText).setAdapter(locationAdapter);
        ((AutoCompleteTextView) locationTagsEditText).setThreshold(1);

        resultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });
    }

    private List<String> getAllTagValues(String type) {
        Set<String> tagValues = new HashSet<>();
        for (Album album : MainActivity.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                for (Tag tag : photo.getTags()) {
                    if (tag.getType().equalsIgnoreCase(type)) {
                        tagValues.add(tag.getValue());
                    }
                }
            }
        }
        return new ArrayList<>(tagValues);
    }    

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void performSearch() {
        String personInput = personTagsEditText.getText().toString().trim();
        String locationInput = locationTagsEditText.getText().toString().trim();

        if (TextUtils.isEmpty(personInput) && TextUtils.isEmpty(locationInput)) {
            Toast.makeText(this, "Enter at least one tag", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] personTags = personInput.isEmpty() ? new String[0] : personInput.split(",");
        String[] locationTags = locationInput.isEmpty() ? new String[0] : locationInput.split(",");

        // Combine all tag inputs into one list of (type, value)
        List<String[]> searchTags = new ArrayList<>();
        for (String p : personTags) {
            searchTags.add(new String[] {"person", p.trim().toLowerCase()});
        }
        for (String l : locationTags) {
            searchTags.add(new String[] {"location", l.trim().toLowerCase()});
        }

        boolean isAndSelected = ((RadioButton)findViewById(R.id.radioAnd)).isChecked();

        List<Photo> matchingPhotos = new ArrayList<>();
        Set<String> seenUris = new HashSet<>();

        for (Album album : MainActivity.getAlbums()) {
            for (Photo photo : album.getPhotos()) {

                Set<String[]> matchedTags = new HashSet<>();
                for (Tag tag : photo.getTags()) {
                    String tagType = tag.getType().toLowerCase();
                    String tagValue = tag.getValue().toLowerCase();

                    for (String[] searchTag : searchTags) {
                        if (tagType.equals(searchTag[0]) && tagValue.startsWith(searchTag[1])) {
                            matchedTags.add(searchTag);
                        }
                    }
                }

                boolean match = isAndSelected
                        ? matchedTags.size() == searchTags.size()
                        : matchedTags.size() > 0;

                if (match && !seenUris.contains(photo.getPhotoUri().toString())) {
                    matchingPhotos.add(photo);
                    seenUris.add(photo.getPhotoUri().toString());
                }
            }
        }

        if (matchingPhotos.isEmpty()) {
            Toast.makeText(this, "No matching photos found", Toast.LENGTH_SHORT).show();
        }

        adapter = new SearchPhotoAdapter(matchingPhotos, this);
        resultsRecyclerView.setAdapter(adapter);
    }
}