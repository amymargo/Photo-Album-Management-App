package app.model;

import java.io.Serializable;
import java.io.File;
import java.time.*;
import java.util.*;

/**
 * Represents a photo with its file path, caption, date of last modification,
 * and associated tags. Tags are stored as a list Tag objects.
 *
 * The modification date is used to simulate the "date taken" for the photo.
 * Supports adding and removing tags, enforcing tag uniqueness based on tag type rules.
 *
 * Photos are considered equal based on their file path.
 * 
 * This class is serializable to allow persistence across sessions.
 * 
 * @author Toma Takamatsu
 */
public class Photo implements Serializable{
    private String filePath;
    private String caption;
    private LocalDateTime dateModified;
    private List<Tag> tags;

    /**
     * Constructs a new Photo with the given file path.
     * Initializes the photo with an empty caption, empty tag list,
     * and sets the last modified time as the photo's date.
     *
     * @param filePath the absolute path to the image file
     */
    public Photo(String filePath){
        // Setting default values of the Photo object
        this.filePath = filePath;
        this.caption = "";
        this.tags = new ArrayList<>();

        // Getting the last modified date
        File file = new File(filePath);
        this.dateModified = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
    }

    /**
     * Returns the file path of the photo.
     *
     * @return the photo's file path as a String
     */
    public String getFilePath(){
        return filePath;
    }

    /**
     * Returns the current caption of the photo.
     *
     * @return the caption string
     */
    public String getCaption(){
        return caption;
    }

    /**
     * Sets a new caption for the photo.
     *
     * @param caption the new caption to be set
     */
    public void setCaption(String caption){
        this.caption = caption;
    }

    /**
     * Returns the last modified date of the photo file.
     * This date is treated as the photo's "taken" date.
     *
     * @return a LocalDateTime representing the modification date
     */
    public LocalDateTime getDateModified(){
        return dateModified;
    }

    /**
     * Returns a list of tags currently associated with the photo.
     *
     * @return list of Tag objects
     */
    public List<Tag> getTags(){
        return tags;
    }

    /**
     * Attempts to add a tag to the photo.
     * If allowsMultiple is false, ensures only one tag per type exists.
     * If the tag already exists or is restricted by uniqueness, it is not added.
     *
     * @param tag the tag to add
     * @param allowsMultiple whether multiple tags of this type are allowed
     * @return true if the tag was added, false otherwise
     */
    public boolean addTag(Tag tag, boolean allowsMultiple){
        String type = tag.getType();
        type = type.toLowerCase();

        if (!allowsMultiple){
            for (Tag t : tags){
                if (t.getType().equals(type)){
                    return false; // Only one of this tag type is allowed
                }
            }
        }

        if (!tags.contains(tag)){
            tags.add(tag);
            return true; // Successfully added the new tag
        }

        return false; // Tag already exists
    }

    /**
     * Removes a tag from the photo if it exists.
     *
     * @param tag the tag to remove
     * @return true if the tag was removed, false if not found
     */
    public boolean removeTag(Tag tag){
        return tags.remove(tag); // Returns true if successfully removed, false if it didn't exist
    }

    /**
     * Returns a list of tags that match the specified type.
     *
     * @param type the tag type to search for
     * @return list of matching tags
     */
    public List<Tag> getTagsByType(String type){
        List<Tag> result = new ArrayList<>();
        for (Tag t : tags){
            if (t.getType().equals(type.trim().toLowerCase())){
                result.add(t); // Adding tag to result if tag matches the type
            }
        }
        return result;
    }

    /**
     * Checks if another object is equal to this photo.
     * Two photos are considered equal if their file paths match.
     *
     * @param o the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Photo)) return false;
        Photo photo = (Photo) o;
        return filePath.equals(photo.filePath); // Comparing filepath to check if the photo is the same
    }

    /**
     * Returns a hash code for the photo, based on its file path.
     *
     * @return the hash code
     */
    @Override
    public int hashCode(){
        return Objects.hash(filePath);
    }
    
}
