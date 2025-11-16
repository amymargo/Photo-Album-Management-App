package app.util;

import app.model.Album;
import app.model.Photo;
import app.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing user accounts, including saving/loading users to disk,
 * creating/deleting users, and initializing the stock user with preloaded photos.
 * This class handles serialization and file system operations for user persistence.
 *
 * The user data is stored as serialized objects under the {@code data/users/} directory.
 * 
 * @author Toma Takamatsu
 */

public class UserManager {
    private static final String USERS_DIR = "data/users/";

    static {
        File dir = new File(USERS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Returns a list of all saved usernames by reading the data directory.
     *
     * @return list of usernames found in the user data directory.
     */

    public static List<String> listUsers() {
        File dir = new File(USERS_DIR);
        String[] files = dir.list((d, name) -> name.endsWith(".dat"));
        List<String> usernames = new ArrayList<>();
        if (files != null) {
            for (String file : files) {
                usernames.add(file.substring(0, file.lastIndexOf('.')));
            }
        }
        return usernames;
    }

    /**
     * Creates a new user and saves it to disk.
     *
     * @param username the username for the new user
     * @return true if the user was created successfully, false if the user already exists or an error occurred.
     */
    public static boolean createUser(String username) {
        File userFile = new File(USERS_DIR + username + ".dat");
        if (userFile.exists()) return false; // user already exists

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(userFile))) {
            out.writeObject(new User(username));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes the user file associated with the given username.
     *
     * @param username the username of the user to delete
     * @return true if the file was deleted successfully, false otherwise.
     */
    public static boolean deleteUser(String username) {
        File userFile = new File(USERS_DIR + username + ".dat");
        return userFile.delete();
    }

    /**
     * Loads a user from disk.
     *
     * @param username the username of the user to load
     * @return the User object if loaded successfully, null otherwise.
     */
    public static User loadUser(String username) {
        File userFile = new File(USERS_DIR + username + ".dat");
        if (!userFile.exists()) return null;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(userFile))) {
            return (User) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Saves the given user object to disk by serializing it.
     *
     * @param user the User object to save
     * @return true if the user was saved successfully, false otherwise.
     */
    public static boolean saveUser(User user) {
        File userFile = new File(USERS_DIR + user.getUsername() + ".dat");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(userFile))) {
            out.writeObject(user);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Initializes the "stock" user with default stock photos if it doesn't already exist.
     * This method is called once at application startup.
     */
    public static void initializeStockUserIfNeeded() {
        // Calling when app is started
        String stockUsername = "stock";
        File userFile = new File(USERS_DIR + stockUsername + ".dat");
        if (userFile.exists()) return; // Already initialized

        User stockUser = new User(stockUsername);
        Album stockAlbum = new Album("stock");

        File stockDir = new File("data/stock");
        File[] stockImages = stockDir.listFiles((dir, name) -> {
            String nameLower = name.toLowerCase();
            return nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") ||
                nameLower.endsWith(".png") || nameLower.endsWith(".gif") ||
                nameLower.endsWith(".bmp");
        });

        if (stockImages != null) {
            for (File file : stockImages) {
                Photo photo = new Photo(file.getPath());
                stockAlbum.addPhoto(photo);
                stockUser.getAllPhotos().add(photo);
            }
        }

        stockUser.getAllAlbums().add(stockAlbum);
        saveUser(stockUser);
    }

}
