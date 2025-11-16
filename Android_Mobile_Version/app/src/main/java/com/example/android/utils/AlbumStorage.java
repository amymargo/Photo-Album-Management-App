package com.example.android.utils;

import android.content.Context;

import com.example.android.Album;

import java.io.*;
import java.util.List;

public class AlbumStorage {
    private static final String FILE_NAME = "albums.dat";

    // Save albums to internal storage
    public static void saveAlbums(Context context, List<Album> albums) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(context.getFilesDir(), FILE_NAME));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(albums);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load albums from internal storage
    public static List<Album> loadAlbums(Context context) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) return null;

            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            List<Album> albums = (List<Album>) ois.readObject();
            ois.close();
            fis.close();
            return albums;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Delete all albums (to reset the app?)
    public static void deleteAlbums(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (file.exists()) file.delete();
    }
}
