package view.controller;

import app.model.Album;
import app.model.Photo;
import app.model.Tag;
import app.model.User;
import app.util.UserManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the album view.
 * Handles displaying, adding, deleting, tagging, moving, and copying photos,
 * as well as caption editing and album creation from search results.
 * 
 * @author Amy Margolina
 */
public class AlbumController {

    // FXML fields from album.fxml
    @FXML private Label albumNameLabel;
    @FXML private ListView<Photo> photoListView;
    @FXML private ImageView photoImageView;
    @FXML private Label captionLabel;
    @FXML private Button captionButton;
    @FXML private Label tagsLabel;
    @FXML private Button addTagButton;
    @FXML private Button removeTagButton;
    @FXML private Button addPhotoButton;
    @FXML private Button deletePhotoButton;
    @FXML private Button backButton;
    @FXML private Button previousPhotoButton;
    @FXML private Button nextPhotoButton;
    @FXML private Label dateLabel;
    @FXML private Button movePhotoButton;
    @FXML private Button copyPhotoButton;
    @FXML private Button createAlbumFromSearchButton;


    // Internal fields
    private User currentUser;
    private Album currentAlbum;
    private ObservableList<Photo> photoObsList = FXCollections.observableArrayList();
    private int currentIndex = -1; // tracks which photo is currently selected

    /**
     * Initializes the album view with user and album data.
     * Sets up the photo list and UI event handlers.
     * 
     * @param user the logged-in user
     * @param album the album to be displayed
     */
    public void initData(User user, Album album) {
        this.currentUser = user;
        this.currentAlbum = album;

        albumNameLabel.setText("Album: " + album.getName());

        // Then load the photos into your ListView, etc.
        loadPhotos();

        // Setup a listener for ListView selection
        photoListView.getSelectionModel().selectedIndexProperty().addListener(
            (obs, oldVal, newVal) -> {
                currentIndex = newVal.intValue();
                showPhotoDetails(currentIndex);
            }
        );

        // Initialize with first photo if available
        if (!photoObsList.isEmpty()) {
            photoListView.getSelectionModel().select(0);
        }

        // Enable "Create Album from Results" button if this is a search results album.
        // You could check by album name or set a flag. Here we assume the temporary search album is named "Search Results".
        if (album.getName().equals("Search Results")) {
            createAlbumFromSearchButton.setVisible(true);
        } else {
            createAlbumFromSearchButton.setVisible(false);
        }
    }

    /**
     * Loads the current album's photos into the list view and sets up cell rendering.
     * Displays caption and image thumbnail.
     */
    public void loadPhotos() {
        photoObsList.setAll(currentAlbum.getPhotos()); // fetch from album
        photoListView.setItems(photoObsList);

        photoListView.setCellFactory(list -> new ListCell<Photo>() {
            private final ImageView thumb = new ImageView();
            @Override
            protected void updateItem(Photo photo, boolean empty) {
                super.updateItem(photo, empty);
                if (empty || photo == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
        
                // 1) Create/load the thumbnail image
                Image img = new Image("file:" + photo.getFilePath(), 80, 60, true, true, true);
                thumb.setImage(img);
                thumb.setFitWidth(60);
                thumb.setFitHeight(40);
                // thumb.setPreserveRatio(true);
        
                // 2) Pick caption or filename
                String caption = photo.getCaption().trim();
                if (caption.isEmpty()) {
                    caption = "(No Caption)";
                }
        
                // 3) Display both image and text
                setText(caption);
                setGraphic(thumb);
            }
        });        

        // Refresh UI if needed
        photoListView.refresh();
    }

    /**
     * Displays the details for the photo at the given index, including image, tags, and metadata.
     * 
     * @param index the index of the photo in the observable list
     */
    private void showPhotoDetails(int index) {
        if (index < 0 || index >= photoObsList.size()) {
            // Clear the display if invalid index
            photoImageView.setImage(null);
            captionLabel.setText("");
            dateLabel.setText("");
            tagsLabel.setText("");
            return;
        }

        Photo photo = photoObsList.get(index);
        // Show the image
        File file = new File(photo.getFilePath());
        if (file.exists()) {
            Image img = new Image(file.toURI().toString());
            photoImageView.setImage(img);
        } else {
            photoImageView.setImage(null);
        }

        // Show the caption
        captionLabel.setText("Caption: " + (photo.getCaption().isEmpty() ? "(No caption)" : photo.getCaption()));

        // Show the date
        dateLabel.setText("Date: " + photo.getDateModified().toString());

        Map<String, List<String>> groupedTags = new HashMap<>();
        for (Tag tag : photo.getTags()) {
            String tagType = tag.getType();
            String tagValue = tag.getValue();
            groupedTags.computeIfAbsent(tagType, k -> new ArrayList<>()).add(tagValue);
        }

        // Display grouped tags
        if (groupedTags.isEmpty()) {
            tagsLabel.setText("Tags: (none)");
        } else {
            StringBuilder tagsText = new StringBuilder("Tags: ");
            for (Map.Entry<String, List<String>> entry : groupedTags.entrySet()) {
                tagsText.append(entry.getKey()).append("=")
                        .append(String.join(", ", entry.getValue()))
                        .append("   ");
            }
            tagsLabel.setText(tagsText.toString().trim());
        }
    }

    /**
     * Opens file chooser to allow the user to import a new photo from disk.
     * Photo path is stored (not copied) and used to track the photo.
     */
    @FXML
    public void addPhoto() {
        // Choosing a photo file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        File file = fileChooser.showOpenDialog(addPhotoButton.getScene().getWindow());
        if (file == null) return;

        // Check if file format is correct
        String name = file.getName().toLowerCase();
        if (!(name.endsWith(".bmp") || name.endsWith(".gif") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) {
            showAlert("Only BMP, GIF, JPEG, and PNG files are allowed.");
            return;
        }

        // Creating new photo object and adding to the album
        Photo photo = new Photo(file.getAbsolutePath());
        if (!currentUser.addPhotoToAlbum(currentAlbum, photo)) {
            showAlert("Photo already exists in this album.");
            return;
        }

        // Saving user info
        UserManager.saveUser(currentUser);
        loadPhotos();
        photoListView.getSelectionModel().select(photo);
    }

    /**
     * Deletes the currently selected photo from the album.
     * If the photo is not in any other album, it is also removed from the user's photo collection.
     */
    @FXML
    public void deletePhoto() {
        // 1) Which photo is selected?
        int selIndex = photoListView.getSelectionModel().getSelectedIndex();
        if (selIndex < 0) {
            showAlert("No photo selected.");
            return;
        }
        Photo photo = photoObsList.get(selIndex);

        // 2) Remove photo from album
        boolean removed = currentAlbum.removePhoto(photo);
        if (!removed) {
            showAlert("Error removing photo (not found in album).");
            return;
        }

        // 3) Check if this photo is in any other album
        boolean stillExistsInOtherAlbum = false;
        for (Album alb : currentUser.getAllAlbums()) {
            if (alb != currentAlbum && alb.getPhotos().contains(photo)) {
                stillExistsInOtherAlbum = true;
                break;
            }
        }
        // If not in any other album, remove from user's allPhotos
        if (!stillExistsInOtherAlbum) {
            currentUser.getAllPhotos().remove(photo);
        }

        // 4) Save
        UserManager.saveUser(currentUser);

        // 5) Reload
        loadPhotos();
        // Reselect something if possible
        if (!photoObsList.isEmpty()) {
            photoListView.getSelectionModel().select(Math.min(selIndex, photoObsList.size() - 1));
        } else {
            showPhotoDetails(-1);
        }
    }

    /**
     * Allows the user to update the caption of the selected photo.
     * Caption updates apply to the photo globally (all albums).
     */
    @FXML
    public void editCaption() {
        // 1) Which photo is selected?
        int selIndex = photoListView.getSelectionModel().getSelectedIndex();
        if (selIndex < 0) {
            showAlert("No photo selected.");
            return;
        }
        Photo photo = photoObsList.get(selIndex);

        // 2) Prompt for new caption
        String newCaption = promptForInput("Enter new caption:");
        if (newCaption == null) {
            return; // user canceled
        }
        photo.setCaption(newCaption.trim());

        // 3) Save
        UserManager.saveUser(currentUser);

        // 4) Refresh display
        loadPhotos();
        photoListView.getSelectionModel().select(selIndex);
        showPhotoDetails(selIndex);
        showAlert("Caption updated.");
    }

    /**
     * Prompts the user to add a tag to the selected photo.
     * Can choose between predefined types (e.g. person, location) or create a custom tag type.
     * Supports single or multiple tag values depending on type.
     */
    @FXML
    public void addTag() {
        int selIndex = photoListView.getSelectionModel().getSelectedIndex();
        if (selIndex < 0) {
            showAlert("No photo selected.");
            return;
        }
        Photo photo = photoObsList.get(selIndex);

        ChoiceDialog<String> typeDialog = new ChoiceDialog<>();
        typeDialog.setTitle("Add Tag");
        typeDialog.setHeaderText("Select or enter a tag type:");
        typeDialog.getItems().addAll(currentUser.getTagTypeRules().keySet());
        typeDialog.getItems().add("Enter new tag type...");

        if (currentUser.getTagTypeRules().isEmpty()) {
            typeDialog.setSelectedItem("Enter new tag type...");
        } else {
            typeDialog.setSelectedItem(currentUser.getTagTypeRules().keySet().iterator().next());
        }

        Optional<String> typeResult = typeDialog.showAndWait();
        if (typeResult.isEmpty()) return;

        String selectedType = typeResult.get().trim().toLowerCase();

        if (selectedType.equals("enter new tag type...")) {
            TextInputDialog newTypeDialog = new TextInputDialog();
            newTypeDialog.setTitle("New Tag Type");
            newTypeDialog.setHeaderText("Enter new tag type:");
            Optional<String> newTypeResult = newTypeDialog.showAndWait();
            if (newTypeResult.isEmpty()) return;

            selectedType = newTypeResult.get().trim().toLowerCase();

            if (currentUser.getTagTypeRules().containsKey(selectedType)) {
                showAlert("This tag type already exists.");
                return;
            }

            Alert multiAlert = new Alert(Alert.AlertType.CONFIRMATION);
            multiAlert.setTitle("Tag Type Rule");
            multiAlert.setHeaderText("Allow multiple values for this tag type?");
            multiAlert.setContentText("Choose Yes to allow multiple tags of this type per photo.");

            ButtonType yes = new ButtonType("Yes");
            ButtonType no = new ButtonType("No");
            multiAlert.getButtonTypes().setAll(yes, no);

            Optional<ButtonType> choice = multiAlert.showAndWait();
            boolean allowsMultiple = choice.isPresent() && choice.get() == yes;

            currentUser.getTagTypeRules().put(selectedType, allowsMultiple);
        }

        TextInputDialog valueDialog = new TextInputDialog();
        valueDialog.setTitle("Tag Value");
        valueDialog.setHeaderText("Enter value for tag '" + selectedType + "':");
        Optional<String> valueResult = valueDialog.showAndWait();
        if (valueResult.isEmpty()) return;

        String value = valueResult.get().trim().toLowerCase();
        boolean allowsMultiple = currentUser.getTagTypeRules().get(selectedType);
        Tag tag = new Tag(selectedType, value);

        if (!photo.addTag(tag, allowsMultiple)) {
            showAlert("Tag already exists.");
            return;
        }

        UserManager.saveUser(currentUser);
        showPhotoDetails(selIndex);
    }

    /**
     * Prompts the user to delete a tag from the selected photo.
     */
    @FXML
    public void deleteTag() {
        // 1) Which photo is selected?
        int selIndex = photoListView.getSelectionModel().getSelectedIndex();
        if (selIndex < 0) {
            showAlert("No photo selected.");
            return;
        }
        Photo photo = photoObsList.get(selIndex);

        // 2) If the photo has no tags, there's nothing to remove
        if (photo.getTags().isEmpty()) {
            showAlert("This photo has no tags.");
            return;
        }

        // 3) Let user pick which tag to remove
        List<Tag> tags = photo.getTags();
        ChoiceDialog<Tag> dialog = new ChoiceDialog<>(tags.get(0), tags);
        dialog.setTitle("Remove Tag");
        dialog.setHeaderText("Select a tag to remove:");
        dialog.setContentText("Tag:");
        Optional<Tag> result = dialog.showAndWait();
        if (!result.isPresent()) {
            return; // user canceled
        }
        Tag toRemove = result.get();

        // 4) Attempt remove
        boolean removed = photo.removeTag(toRemove);
        if (!removed) {
            showAlert("Tag not found or couldn't be removed.");
        } else {
            showAlert("Tag removed.");
        }

        // 5) Save and refresh
        UserManager.saveUser(currentUser);
        showPhotoDetails(selIndex);
    }

    /**
     * Moves the selected photo to another album (removes from current album).
     */
    @FXML
    public void movePhoto() {
        int selIndex = photoListView.getSelectionModel().getSelectedIndex();
        if (selIndex < 0) {
            showAlert("No photo selected.");
            return;
        }
        Photo photo = photoObsList.get(selIndex);

        // Collect album names except the current album
        List<String> albumNames = new ArrayList<>();
        for (Album album : currentUser.getAllAlbums()) {
            if (!album.equals(currentAlbum)) {
                albumNames.add(album.getName());
            }
        }

        if (albumNames.isEmpty()) {
            showAlert("There are no other albums.");
            return;
        }

        // Show dropdown of albums
        ChoiceDialog<String> albumDialog = new ChoiceDialog<>(albumNames.get(0), albumNames);
        albumDialog.setTitle("Move Photo");
        albumDialog.setHeaderText("Select an album to move the photo to:");
        albumDialog.setContentText("Album:");

        Optional<String> result = albumDialog.showAndWait();
        if (result.isEmpty()) return;

        String targetAlbumName = result.get();
        Album targetAlbum = currentUser.getAlbumByName(targetAlbumName);
        if (targetAlbum == null) {
            showAlert("Album not found.");
            return;
        }

        if (!currentUser.movePhoto(currentAlbum, photo, targetAlbum)) {
            showAlert("Photo already exists in the selected album.");
            return;
        }

        UserManager.saveUser(currentUser);

        loadPhotos(); // Refresh list after moving
        showAlert("Photo moved to " + targetAlbumName + ".");

        if (!photoObsList.isEmpty()) {
            photoListView.getSelectionModel().select(Math.min(selIndex, photoObsList.size() - 1));
        } else {
            showPhotoDetails(-1);
        }
    }

    /**
     * Copies the selected photo to another album (adds reference, does not duplicate file).
     * Allows a photo to exist in multiple albums.
     */
    @FXML
    public void copyPhoto() {
        int selIndex = photoListView.getSelectionModel().getSelectedIndex();
        if (selIndex < 0) {
            showAlert("No photo selected.");
            return;
        }
        Photo photo = photoObsList.get(selIndex);
    
        List<String> albumNames = new ArrayList<>();
        for (Album album : currentUser.getAllAlbums()) {
            if (!album.equals(currentAlbum)) {
                albumNames.add(album.getName());
            }
        }
    
        if (albumNames.isEmpty()) {
            showAlert("There are no other albums.");
            return;
        }
    
        ChoiceDialog<String> albumDialog = new ChoiceDialog<>(albumNames.get(0), albumNames);
        albumDialog.setTitle("Copy Photo");
        albumDialog.setHeaderText("Select an album to copy the photo to:");
        albumDialog.setContentText("Album:");
    
        Optional<String> result = albumDialog.showAndWait();
        if (result.isEmpty()) return;
    
        String targetAlbumName = result.get();
        Album targetAlbum = currentUser.getAlbumByName(targetAlbumName);
        if (targetAlbum == null) {
            showAlert("Album not found.");
            return;
        }
    
        if (!currentUser.addPhotoToAlbum(targetAlbum, photo)) {
            showAlert("Photo already exists in the selected album.");
            return;
        }
    
        UserManager.saveUser(currentUser);
    
        showAlert("Photo copied to " + targetAlbumName + ".");
    }    

    /**
     * Navigates to the previous photo in the album with wrap-around.
     */
    @FXML
    public void navigateLeft() {
        // Go to previous photo
        if (photoObsList.isEmpty()) {
            return;
        }
        currentIndex = (currentIndex - 1 + photoObsList.size()) % photoObsList.size();
        photoListView.getSelectionModel().select(currentIndex);
    }

    /**
     * Navigates to the next photo in the album with wrap-around.
     */
    @FXML
    public void navigateRight() {
        // Go to next photo
        if (photoObsList.isEmpty()) {
            return;
        }
        currentIndex = (currentIndex + 1) % photoObsList.size();
        photoListView.getSelectionModel().select(currentIndex);
    }

    /**
     * Navigates back to the user dashboard from the album view.
     */
    @FXML
    public void goBack() {
        // Return to user screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/user.fxml"));
            Parent root = loader.load();
            UserController userController = loader.getController();
            userController.setUser(currentUser);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("Welcome, " + currentUser.getUsername());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error returning to user screen.");
        }
    }

    /**
     * Creates a new album containing the current set of photos from search results.
     */
    @FXML
    private void createAlbumFromSearch() {
        // Prompt the user for a new album name.
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Album");
        dialog.setHeaderText("Enter a new album name for the search results:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) {
            showAlert("Album name cannot be empty.");
            return;
        }
        String newName = result.get().trim();

        // Use the user's method to create a new album with these photos.
        // This assumes you have createAlbumWithPhotos(String, List<Photo>) in your User model.
        boolean success = currentUser.createAlbumWithPhotos(newName, currentAlbum.getPhotos());
        if (!success) {
            showAlert("An album with that name already exists.");
            return;
        }

        // Save the updated user data.
        UserManager.saveUser(currentUser);
        
        showAlert("New album '" + newName + "' created from search results.");
        
        // Optionally, navigate back to the user screen.
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/user.fxml"));
            Parent root = loader.load();
            UserController userController = loader.getController();
            userController.setUser(currentUser);

            Stage stage = (Stage) createAlbumFromSearchButton.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("Welcome, " + currentUser.getUsername());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error navigating back to the user screen.");
        }
    }

    /**
     * Displays a simple Alert box with an OK button.
     * @param message the message to display
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a TextInputDialog to prompt the user for input.
     * 
     * @param prompt the text to display in the dialog
     * @return the user's input or null if canceled
     */
    private String promptForInput(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input Required");
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
}