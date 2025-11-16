package view.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import java.util.List;
import app.util.UserManager;

/**
 * Controller for the admin panel.
 * Allows the admin to create and delete users, and view the user list.
 * 
 * @author Amy Margolina
 */
public class AdminController {
    @FXML private ListView<String> userListView;
    @FXML private TextField userTextField;
    @FXML private Button createUserButton;
    @FXML private Button deleteUserButton;
    @FXML private Button logoutButton;
    @FXML private Label statusLabel;

    /**
     * Initializes the admin controller.
     * Loads the user list and sets up selection listener.
     */
    public void initialize() {
        refreshUserList();
    
        //make it so when someone clicks an item in the list, the name gets filled in the search bar (so it can easily be deleted)
        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                userTextField.setText(newValue);
            } else {
                userTextField.clear();
            }
        });
    }    

    /**
     * Refreshes the user list displayed in the ListView.
     */
    private void refreshUserList(){
        // Private method to refresh the list of users
        List<String> users = UserManager.listUsers();
        userListView.getItems().setAll(users);
    }

    /**
     * Creates a new user with the username provided in the text field.
     */
    @FXML
    private void handleCreateUser(){
        // Getting the username from the textfield
        String username = userTextField.getText().trim();
        if (username.isEmpty()){ // Checking to see if the textfield was empty or not
            statusLabel.setText("Username cannot be empty.");
            return;
        }

        // Making sure username does not have spaces or special symbols
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            statusLabel.setText("Username cannot contain spaces or special symbols.");
            return;
        }

        // Seeing if we were able to create the user successfully
        boolean success = UserManager.createUser(username);
        if (success){
            statusLabel.setText("User '" + username + "' has been created.");
            refreshUserList();
        }
        else{
            statusLabel.setText("User '" + username + "' already exists.");
        }
    }

    /**
     * Deletes the user specified in the text field (or selected, since when you select a user, the name appears in the text field).
     */
    @FXML
    private void handleDeleteUser(){
        // Getting the username from the textfield
        String username = userTextField.getText().trim();
        if (username.isEmpty()) { // Checking to see if the textfield was empty or not
            statusLabel.setText("Username cannot be empty.");
            return;
        }

        // Making sure username does not have spaces or special symbols
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            statusLabel.setText("Username cannot contain spaces or special symbols.");
            return;
        }

        // Seeing if we were able to delete the user successfully
        boolean success = UserManager.deleteUser(username);
        if (success) {
            statusLabel.setText("User '" + username + "' has been deleted.");
            refreshUserList();
        } else {
            statusLabel.setText("User '" + username + "' does not exist.");
        }
    }

    /**
     * Logs out the admin and returns to the login screen.
     */
    @FXML
    private void handleLogout(){

        // Loading the login screen again
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 400);
            stage.setTitle("Login Menu");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("Error loading login screen.");
            e.printStackTrace();  // Consider removing in final build (GUI-only)
        }
    }

}