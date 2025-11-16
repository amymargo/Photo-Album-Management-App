package com.example.android;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;


public class Album implements Serializable{
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Photo> photos;


    public Album(String name){
        this.name = name.trim();
        this.photos = new ArrayList<>();
    }

    public String getName(){
        return name;
    }

    public void setName(String newName){
        this.name = newName.trim();
    }

    public List<Photo> getPhotos(){
        return photos;
    }

    public boolean addPhoto(Photo photo){
        if (photos.contains(photo)) return false; // Photo already in album

        photos.add(photo);
        return true;
    }

    public boolean removePhoto(Photo photo){
        return photos.remove(photo); // False if photo is not in album
    }

    public int getPhotoCount(){
        return photos.size();
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Album)) return false;
        Album album = (Album) o;
        return name.equals(album.getName()); // Not going to ignore cases since albums could mean different things depending on case
    }

    @Override
    public int hashCode(){
        return Objects.hash(name);
    }

    @Override
    public String toString(){
        return name;
    }

}
