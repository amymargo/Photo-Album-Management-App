package com.example.android;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.Serializable;
import java.io.File;
import java.time.*;
import java.util.*;

public class Photo implements Serializable{
    private String photoUriString;
    private String caption;
    private List<Tag> tags;

    public Photo(Uri photoUri, Context context) {
        this.photoUriString = photoUri.toString();
        this.caption = extractFileName(photoUri, context);  // Use filename as caption
        this.tags = new ArrayList<>();
    }

    private String extractFileName(Uri uri, Context context) {
        String result = "unknown";
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex >= 0) {
                result = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return result;
    }

    public Uri getPhotoUri() { return Uri.parse(photoUriString); }
    public String getCaption(){
        return caption;
    }
    public List<Tag> getTags(){
        return tags;
    }

    public boolean addTag(Tag tag){
        String type = tag.getType();
        type = type.toLowerCase();

        if (type.equals("location")){ // if it is a location tag, only one is allowed
            for (Tag t : tags){
                if(t.getType().equals(type)){
                    return false;
                }
            }
        }

        if (!tags.contains(tag)){
            tags.add(tag);
            return true;
        }

        return false;

    }

    public boolean removeTag(Tag tag){
        return tags.remove(tag); // Returns true if successfully removed, false if it didn't exist
    }

    public List<Tag> getTagsByType(String type){
        List<Tag> result = new ArrayList<>();
        for (Tag t : tags){
            if (t.getType().equals(type.trim().toLowerCase())){
                result.add(t); // Adding tag to result if tag matches the type
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Photo)) return false;
        Photo photo = (Photo) o;
        return photoUriString.equals(photo.photoUriString); // Comparing filepath to check if the photo is the same
    }

    @Override
    public int hashCode(){
        return Objects.hash(photoUriString);
    }
    
}
