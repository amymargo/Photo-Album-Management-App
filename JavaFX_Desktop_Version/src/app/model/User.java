package app.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a user in the photo album application.
 * A user can manage albums, photos, and custom tag rules.
 * Implements Serializable for persistent local storage.
 * 
 * @author Toma Takamatsu
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private List<Album> allAlbums;
    private List<Photo> allPhotos;
    private Map<String, Boolean> tagTypeRules; // true = multiple allowed, false = unique


    /**
     * Constructs a new user with default tag rules ("person" and "location").
     * Initializes an empty list for albums and photos
     *
     * @param username the unique username for the user
     */

    public User(String username) {
        this.username = username;
        this.allAlbums = new ArrayList<>();
        this.allPhotos = new ArrayList<>();
        this.tagTypeRules = new HashMap<>();

        // Default tags
        tagTypeRules.put("person", true);
        tagTypeRules.put("location", false);
    }

    /**
     * Gets the username of this user.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the list of all albums associated with this user.
     *
     * @return the list of albums
     */
    public List<Album> getAllAlbums() {
        return allAlbums;
    }

    /**
     * Gets the list of all photos associated with this user.
     *
     * @return the list of photos
     */
    public List<Photo> getAllPhotos() {
        return allPhotos;
    }

    /**
     * Gets the map of tag type rules, where true = multiple values allowed,
     * and false = only one value allowed.
     *
     * @return the tag type rules map
     */
    public Map<String, Boolean> getTagTypeRules(){
        return tagTypeRules;
    }

    /**
     * Recalculates the photo list by scanning all albums.
     */
    private void refreshPhotoList(){
        // Iterates through all albums and adds each one to the list
        allPhotos = new ArrayList<>();
        for (Album a : allAlbums){
            for (Photo p : a.getPhotos()){
                allPhotos.add(p);
            }
        }
    }

    /**
     * Creates an empty album for the user.
     *
     * @param albumName the name of the album to create
     * @return true if the album was successfully created, false if it already exists
     */
    public boolean createAlbum(String albumName){
        albumName = albumName.trim();
        if (getAlbumByName(albumName) != null) return false; // Album name exists already

        Album newAlbum = new Album(albumName);
        allAlbums.add(newAlbum);
        return true;
    }

    /**
     * Creates a new album with the provided photos.
     *
     * @param albumName the name of the new album
     * @param photosToAdd the photos to include in the new album
     * @return true if created successfully, false if album name already exists
     */
    public boolean createAlbumWithPhotos(String albumName, List<Photo> photosToAdd){
        albumName = albumName.trim();
        if (getAlbumByName(albumName) != null) return false; // Album name exists already

        Album newAlbum = new Album(albumName);
        for (Photo p : photosToAdd){
            newAlbum.addPhoto(p); // Adding each photo to new album
            if (!allPhotos.contains(p)) allPhotos.add(p); // Adding photo to photo list if it is new
        }
        allAlbums.add(newAlbum);
        return true;
    }

    /**
     * Deletes the specified album and refreshes the user's photo list.
     *
     * @param album the album to delete
     */
    public void deleteAlbum(Album album){
        allAlbums.remove(album);
        refreshPhotoList();
    }

    /**
     * Renames an album if the new name does not already exist.
     *
     * @param album the album to rename
     * @param newName the new name for the album
     * @return true if renamed, false if a name conflict exists
     */
    public boolean renameAlbum(Album album, String newName){
        for (Album a : allAlbums){
            if (a.getName().equals(newName)){
                return false;
            }
        }
        album.setName(newName);
        return true;
    }

    /**
     * Searches for an album by name.
     *
     * @param albumName the name of the album to find
     * @return the album if found, otherwise null
     */
    public Album getAlbumByName(String albumName){
        albumName = albumName.trim();
        for (Album a : allAlbums){
            if (a.getName().equals(albumName)){
                return a;
            }
        }
        return null; // Album with name not found
    }
    
    /**
     * Adds a photo to a specified album and user's photo list if new.
     *
     * @param album the target album
     * @param photo the photo to add
     * @return true if the photo was added successfully, false otherwise
     */
    public boolean addPhotoToAlbum(Album album, Photo photo){
        for (Photo p : allPhotos){
            if (p.equals(photo)){
                photo = p; // Checking if photo already exists for user
            }
        }
        boolean added = album.addPhoto(photo); // Adding to album, false if photo already existed in album
        if (added && !allPhotos.contains(photo)){
            allPhotos.add(photo); // Adding to photo list if it is new
        }
        return added;
    }

    /**
     * Removes a photo from an album and from the user's photo list if no other albums contain it.
     *
     * @param album the album to remove from
     * @param photo the photo to remove
     * @return true if removed, false if it wasn't in the album
     */
    public boolean removePhotoFromAlbum(Album album, Photo photo){
        boolean removed = album.removePhoto(photo);
        if (!removed) return removed;
        for (Album a : allAlbums){
            if (a.getPhotos().contains(photo)) return removed; // Checking if any of the albums have the photo
        }
        allPhotos.remove(photo); // If no albums have the photo, the photo is deleted from the user
        return removed;
    }

    /**
     * Removes the specified photo from all albums and the user’s list.
     *
     * @param photo the photo to remove
     */
    public void removePhotoFromAll(Photo photo){
        allPhotos.remove(photo);
        for (Album a : allAlbums){
            a.getPhotos().remove(photo);
        }
    }

    /**
     * Moves a photo from one album to another.
     *
     * @param fromAlbum the source album
     * @param photo the photo to move
     * @param toAlbum the destination album
     * @return true if the photo was moved, false if destination already had it
     */
    public boolean movePhoto(Album fromAlbum, Photo photo, Album toAlbum){
        if (!toAlbum.addPhoto(photo)) return false; // toAlbum already has the photo
        fromAlbum.removePhoto(photo);
        return true; // Successfully moved photo
    }


    /**
     * Adds a new tag to the photo and updates the tag type rule if it's a new type.
     *
     * @param photo the photo to tag
     * @param type the tag type
     * @param value the tag value
     * @param multipleAllowed whether this tag type allows multiple values
     * @return true if the tag was added, false if it violates rules
     */
    public boolean createNewTag(Photo photo, String type, String value, boolean multipleAllowed){
        // This method will be called if the tag type is new
        type = type.trim().toLowerCase();
        value = value.trim().toLowerCase();

        tagTypeRules.put(type, multipleAllowed); // Keeping list of tag types 
        return photo.addTag(new Tag(type, value), multipleAllowed); // Adding tag to photo
    }

    /**
     * Updates the uniqueness rule for a tag type.
     *
     * @param type the tag type
     * @param multipleAllowed true if multiple values should be allowed
     * @return true if updated, false if tag type doesn't exist
     */
    public boolean changeTagTypeUniqueness(String type, boolean multipleAllowed){
        // To change whether multiple of a tag type is allowed or not
        if (!tagTypeRules.containsKey(type)) return false;
        tagTypeRules.replace(type, multipleAllowed);
        return true;
    }

    /**
     * Filters photos within a given date range.
     *
     * @param photoList the list of photos to search
     * @param start the start of the date range (inclusive)
     * @param end the end of the date range (inclusive)
     * @return list of photos in the given range
     */
    public List<Photo> searchByDateRange(List<Photo> photoList, LocalDateTime start, LocalDateTime end){
        List<Photo> result = new ArrayList<>();
        for (Photo p : photoList){
            if ((p.getDateModified().isAfter(start) && p.getDateModified().isBefore(end))||(p.getDateModified().isEqual(start))||(p.getDateModified().isEqual(end))){
                result.add(p); // If photo date is in range(inclusive), we add the photo
            }
        }
        return result;
    }

    /**
     * Searches for photos that contain a specific tag.
     *
     * @param type the tag type
     * @param value the tag value
     * @param photoList the list of photos to search
     * @return list of photos that contain the tag
     */
    public List<Photo> searchByTag(String type, String value, List<Photo> photoList){
        type = type.trim().toLowerCase();
        value = value.trim().toLowerCase();
        // Creating tag to search for
        Tag tag = new Tag(type, value);

        List<Photo> result = new ArrayList<>();
        for (Photo p : photoList){
            if (p.getTags().contains(tag)){
                // Add to result if the photo contains the tag
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Searches for photos using two tag conditions, either conjunctive (AND) or disjunctive (OR).
     *
     * @param type1 first tag type
     * @param value1 first tag value
     * @param type2 second tag type
     * @param value2 second tag value
     * @param isConjunctive true for AND search, false for OR
     * @param photoList list of photos to search
     * @return list of photos matching the condition
     */
    public List<Photo> searchByTag(String type1, String value1, String type2, String value2, boolean isConjunctive, List<Photo> photoList){
        type1 = type1.trim().toLowerCase();
        value1 = value1.trim().toLowerCase();
        type2 = type2.trim().toLowerCase();
        value2 = value2.trim().toLowerCase();

        // Creating the tags to search for
        Tag tag1 = new Tag(type1, value1);
        Tag tag2 = new Tag(type2, value2);

        List<Photo> result = new ArrayList<>();
        for (Photo p : photoList){
            // Check to see if photo has the tags
            boolean hasTag1 = p.getTags().contains(tag1);
            boolean hasTag2 = p.getTags().contains(tag2);

            if (isConjunctive && hasTag1 && hasTag2){ // If conjunctive
                result.add(p);
            }
            else if (!isConjunctive && (hasTag1 || hasTag2)){ // If disjunctive
                result.add(p);
            }
        }
        return result;
    }
}
