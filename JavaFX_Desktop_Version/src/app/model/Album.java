package app.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents an album which contains a collection of photos.
 * Each album keeps track of its own name, list of photos, and 
 * the date range based on the earliest and latest photos.
 *
 * Provides functionality to add and remove photos, and keeps
 * the date range updated accordingly.
 *
 * Albums are compared by their names, and the name must be unique per user.
 * 
 * @author Toma Takamatsu
 */
public class Album implements Serializable{
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Photo> photos;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    /**
     * Constructs a new Album with the given album name.
     * Initializes the album with an empty photo list and null start/end dates.
     *
     * @param name the name of the album
     */
    public Album(String name){
        this.name = name.trim();
        this.photos = new ArrayList<>();
        this.startDate = null;
        this.endDate = null;
    }

    /**
     * Returns the name of the album.
     *
     * @return the album name as a String
     */
    public String getName(){
        return name;
    }

    /**
     * Sets a new name for the album.
     *
     * @param newName the new album name
     */
    public void setName(String newName){
        this.name = newName.trim();
    }

    /**
     * Returns the list of photos in the album.
     *
     * @return list of Photo objects
     */
    public List<Photo> getPhotos(){
        return photos;
    }

    /**
     * Adds a photo to the album if it is not already present.
     * Updates the album's start and end dates accordingly.
     *
     * @param photo the photo to add
     * @return true if the photo was added, false if it already exists
     */
    public boolean addPhoto(Photo photo){
        if (photos.contains(photo)) return false; // Photo already in album

        photos.add(photo);
        updateDateRangeOnAdd(photo); // Updating start/end dates
        return true;
    }

    /**
     * Removes a photo from the album if it exists.
     * If the removed photo affected the date range, the range is recalculated.
     *
     * @param photo the photo to remove
     * @return true if the photo was removed, false if not found
     */
    public boolean removePhoto(Photo photo){
        if (!photos.remove(photo)) return false; // Photo not in album

        updateDateRangeOnRemoval(photo); // Updating start/end dates
        return true;
    }

    /**
     * Returns the number of photos currently in the album.
     *
     * @return the size of the photo list
     */
    public int getPhotoCount(){
        return photos.size();
    }

    /**
     * Returns the earliest date among the photos in the album.
     *
     * @return the start date or null if the album is empty
     */
    public LocalDateTime getStartDate(){
        return startDate;
    }

    /**
     * Returns the latest date among the photos in the album.
     *
     * @return the end date or null if the album is empty
     */
    public LocalDateTime getEndDate(){
        return endDate;
    }

    /**
     * Returns the date range of the album in string format.
     *
     * @return a string showing the range, or "Album is empty" if there are no photos
     */
    public String getDateRange(){
        if (startDate == null || endDate == null) return "Album is empty"; 
        return startDate.toLocalDate() + " to " + endDate.toLocalDate(); // Returning string for display
    }

    /**
     * Updates the start or end date of the album when a new photo is added.
     * Only updates if the new photo's date is outside the current range.
     *
     * @param photo the photo being added
     */
    private void updateDateRangeOnAdd(Photo photo){
        LocalDateTime date = photo.getDateModified(); // Updating if photo would be the new start/end date

        if (startDate == null || date.isBefore(startDate)){
            startDate = date;
        }
        if (endDate == null || date.isAfter(endDate)){
            endDate = date;
        }
    }

    /**
     * Checks if the removed photo was affecting the date range.
     * Recalculates the full range if needed.
     *
     * @param photo the photo being removed
     */
    private void updateDateRangeOnRemoval(Photo photo){
        LocalDateTime date = photo.getDateModified();
        if (startDate.equals(date) || endDate.equals(date)){ // Checking if photo is the start/end date
            recalculateDateRange();
        }
    }

    /**
     * Recalculates the start and end dates by iterating through all photos.
     * Called when the date range becomes invalid (e.g., after a removal).
     */
    private void recalculateDateRange(){
        startDate = null;
        endDate = null;
        for (Photo p : photos){
            updateDateRangeOnAdd(p);
        }
    }

    /**
     * Compares this album to another object.
     * Albums are considered equal if their names are the same.
     *
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Album)) return false;
        Album album = (Album) o;
        return name.equals(album.getName()); // Not going to ignore cases since albums could mean different things depending on case
    }

    /**
     * Returns the hash code for the album based on its name.
     *
     * @return the hash code
     */
    @Override
    public int hashCode(){
        return Objects.hash(name);
    }

    /**
     * Returns the album name as its string representation.
     *
     * @return the album name
     */
    @Override
    public String toString(){
        return name;
    }

}
