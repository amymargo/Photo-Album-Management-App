package view.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import app.model.Album;
import app.model.Photo;
import app.model.User;
import app.util.UserManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for the user dashboard.
 * Manages the user's albums and provides functionality to create, delete, rename, open, and search albums.
 * 
 * @author Amy Margolina
 */
public class UserController {
    @FXML private Label welcomeLabel;
    @FXML private TableView<Album> albumTableView;
    @FXML private TableColumn<Album, String> nameColumn;
    @FXML private TableColumn<Album, Integer> countColumn;
    @FXML private TableColumn<Album, String> rangeColumn;
    @FXML private TextField albumNameField;
    @FXML private Button createAlbumButton;
    @FXML private Button deleteAlbumButton;
    @FXML private Button renameAlbumButton;
    @FXML private Button openAlbumButton;
    @FXML private Button logoutButton;

    private User currentUser;
    private ObservableList<Album> albumDisplayList;
    
    /**
     * Sets the current user and loads their albums into the table.
     * @param user the logged-in user
     */
    public void setUser(User user){
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        loadAlbums();
    }

    /**
     * Loads the user's albums into the table view.
     */
    private void loadAlbums(){
        albumDisplayList = FXCollections.observableArrayList(currentUser.getAllAlbums());
        albumTableView.setItems(albumDisplayList);
        albumTableView.refresh();
    }

    /**
     * Initializes table columns for album name, photo count, and date range.
     */
    @FXML
    private void initialize(){
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        countColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPhotoCount()).asObject());
        rangeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateRange()));
    }

    /**
     * Creates a new album with the entered name if it doesn't already exist.
     */
    @FXML
    private void createAlbum() {
        String name = albumNameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Album name cannot be empty.");
            return;
        }
        if (!currentUser.createAlbum(name)) {
            showAlert("An album with that name already exists.");
        } else {
            UserManager.saveUser(currentUser);
            albumNameField.clear();
            loadAlbums();
        }
    }

    /**
     * Deletes the selected album from the list.
     */
    @FXML
    private void deleteAlbum() {
        Album selected = albumTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select an album to delete.");
            return;
        }
        currentUser.deleteAlbum(selected);
        UserManager.saveUser(currentUser);
        loadAlbums();
    }

    /**
     * Renames the selected album to the entered new name, if unique.
     */
    @FXML
    private void renameAlbum() {
        Album selected = albumTableView.getSelectionModel().getSelectedItem();
        String newName = albumNameField.getText().trim();
        if (selected == null) {
            showAlert("Select an album to rename.");
            return;
        }
        if (newName.isEmpty()) {
            showAlert("New album name cannot be empty.");
            return;
        }
        if (!currentUser.renameAlbum(selected, newName)) {
            showAlert("An album with that name already exists.");
        } else {
            UserManager.saveUser(currentUser);
            albumNameField.clear();
            loadAlbums();
        }
    }

    /**
     * Opens the selected album and switches to the album view.
     */
    @FXML
    private void openAlbum() {
        Album selected = albumTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No album selected.");
            return;
        }
    
        try {
            // 1) Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/album.fxml"));
            Parent root = loader.load();
    
            // 2) Get the controller
            AlbumController albumCtrl = loader.getController();
    
            // 3) Pass in the needed data via your new initData method
            albumCtrl.initData(currentUser, selected);
    
            // 4) Show the album scene
            Stage stage = (Stage) openAlbumButton.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("Album: " + selected.getName()); 
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error loading album screen.");
        }
    }     

    /**
     * Saves the current session and logs the user out, returning to the login screen.
     * @throws Exception if FXML fails to load
     */
    @FXML
    private void logout() throws Exception {
        UserManager.saveUser(currentUser);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();

        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Login Menu");
        stage.show();
    }

    /**
     * Allows the user to search photos either by date range, single tag, or tag combinations (AND/OR).
     * Search results are shown in a temporary album which can be saved.
     * 
     * @throws IOException if FXML loading fails
     */
    @FXML
    private void searchPhotos() throws IOException {
        // 1. Ask the user which search method to use.
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Date Range", "Date Range", "Single Tag", "Two Tags (AND)", "Two Tags (OR)");
        dialog.setTitle("Search Photos");
        dialog.setHeaderText("Choose Search Method");
        Optional<String> choice = dialog.showAndWait();
        if (choice.isEmpty()) {
            return; // user cancelled
        }
        String selection = choice.get();

        // 2. Prepare a container for matching photos.
        List<Photo> resultList = new ArrayList<>();

        // We'll search through all photos of the user.
        List<Photo> allUserPhotos = currentUser.getAllPhotos();

        // 3. Based on selection, gather search parameters and perform search.
        if (selection.equals("Date Range")) {
            // Prompt for start and end dates (using format yyyy-MM-dd)
            LocalDate startDate = promptForDate("Select Start Date:");
            if (startDate == null) return;
            
            LocalDate endDate = promptForDate("Select End Date:");
            if (endDate == null) return;
            
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(23, 59, 59); // full end of the day
            
            resultList = currentUser.searchByDateRange(allUserPhotos, start, end);            
            
            // Use the User model method to search by date range.
            resultList = currentUser.searchByDateRange(allUserPhotos, start, end);
            
        } else if (selection.equals("Single Tag")) {
            // Tag Type ChoiceDialog
            ChoiceDialog<String> typeDialog = new ChoiceDialog<>();
            typeDialog.setTitle("Search");
            typeDialog.setHeaderText("Select a tag type:");
            typeDialog.getItems().addAll(currentUser.getTagTypeRules().keySet());
            if (currentUser.getTagTypeRules().isEmpty()) {
                showAlert("No tag types available.");
                return;
            } else {
                typeDialog.setSelectedItem(currentUser.getTagTypeRules().keySet().iterator().next());
            }

            Optional<String> typeResult = typeDialog.showAndWait();
            if (typeResult.isEmpty()) return;
            String type = typeResult.get().trim().toLowerCase();

            // Tag Value TextInputDialog
            TextInputDialog valueDialog = new TextInputDialog();
            valueDialog.setTitle("Search");
            valueDialog.setHeaderText("Enter tag value:");
            Optional<String> valueResult = valueDialog.showAndWait();
            if (valueResult.isEmpty()) return;
            String value = valueResult.get().trim().toLowerCase();
            
            // Search by single tag.
            resultList = currentUser.searchByTag(type, value, allUserPhotos);
            
        } else if (selection.equals("Two Tags (AND)") || selection.equals("Two Tags (OR)")) {
            // First Tag Type ChoiceDialog
            ChoiceDialog<String> type1Dialog = new ChoiceDialog<>();
            type1Dialog.setTitle("Search");
            type1Dialog.setHeaderText("Select first tag type:");
            type1Dialog.getItems().addAll(currentUser.getTagTypeRules().keySet());
            if (currentUser.getTagTypeRules().isEmpty()) {
                showAlert("No tag types available.");
                return;
            } else {
                type1Dialog.setSelectedItem(currentUser.getTagTypeRules().keySet().iterator().next());
            }

            Optional<String> type1Result = type1Dialog.showAndWait();
            if (type1Result.isEmpty()) return;
            String type1 = type1Result.get().trim().toLowerCase();

            // First Tag Value
            TextInputDialog value1Dialog = new TextInputDialog();
            value1Dialog.setTitle("Search");
            value1Dialog.setHeaderText("Enter value for first tag:");
            Optional<String> value1Result = value1Dialog.showAndWait();
            if (value1Result.isEmpty()) return;
            String value1 = value1Result.get().trim().toLowerCase();

            // Second Tag Type ChoiceDialog
            ChoiceDialog<String> type2Dialog = new ChoiceDialog<>();
            type2Dialog.setTitle("Search");
            type2Dialog.setHeaderText("Select second tag type:");
            type2Dialog.getItems().addAll(currentUser.getTagTypeRules().keySet());
            type2Dialog.setSelectedItem(currentUser.getTagTypeRules().keySet().iterator().next());

            Optional<String> type2Result = type2Dialog.showAndWait();
            if (type2Result.isEmpty()) return;
            String type2 = type2Result.get().trim().toLowerCase();

            // Second Tag Value
            TextInputDialog value2Dialog = new TextInputDialog();
            value2Dialog.setTitle("Search");
            value2Dialog.setHeaderText("Enter value for second tag:");
            Optional<String> value2Result = value2Dialog.showAndWait();
            if (value2Result.isEmpty()) return;
            String value2 = value2Result.get().trim().toLowerCase();
            
            // Determine if the search is conjunctive (AND) or disjunctive (OR).
            boolean isConjunctive = selection.equals("Two Tags (AND)");
            resultList = currentUser.searchByTag(type1, value1, type2, value2, isConjunctive, allUserPhotos);
        }

        // 4. Check if any photos matched.
        if (resultList.isEmpty()) {
            showAlert("No photos found matching your search.");
            return;
        }

        // 5. Create a temporary "Search Results" album (do not add this permanently to the user's album list).
        Album searchResultsAlbum = new Album("Search Results");
        for (Photo p : resultList) {
            searchResultsAlbum.addPhoto(p);
        }

        // 6. Load the album screen (album.fxml) and pass the search results album.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/album.fxml"));
        Parent root = loader.load();
        AlbumController albumCtrl = loader.getController();
        albumCtrl.initData(currentUser, searchResultsAlbum);
        
        // Optional: If you want to enable special "search mode" features (like a 'Create Album from Results' button),
        // you could call a method like albumCtrl.enableSearchAlbumMode(); if implemented.

        Stage stage = (Stage) albumTableView.getScene().getWindow();
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Search Results");
        stage.show();
    }

    /**
     * Prompts the user to select a date using a DatePicker.
     * Used for search by date range.
     * 
     * @param prompt the message to show in the dialog
     * @return the selected LocalDate, or null if canceled
     */
    private LocalDate promptForDate(String prompt) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Select Date");
        dialog.setHeaderText(prompt);
    
        DatePicker datePicker = new DatePicker();
        dialog.getDialogPane().setContent(datePicker);
    
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
    
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return datePicker.getValue();
            }
            return null;
        });
    
        Optional<LocalDate> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Displays an alert box with the given message.
     * 
     * @param message the message to display
     */
    private void showAlert(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Alert");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
