package view.controller;

import app.model.*;
import app.util.UserManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the login screen.
 * Handles user authentication and navigation to the appropriate dashboard (admin or user).
 * 
 * @author Amy Margolina
 */
public class LoginController {

    @FXML private Label lblStatus;
    @FXML private TextField txtUser;
    @FXML private Button btnLogin;

    /**
     * Handles login attempts when the login button is pressed.
     * Loads the admin view if "admin" is entered, otherwise attempts to load the user data and open user dashboard.
     * 
     * @param e the action event triggered by the login button
     * @throws Exception if FXML loading fails
     */
    public void Login(ActionEvent e) throws Exception{
        String username = txtUser.getText().trim();

        if (username.equals("admin")){ // Loading admin panel
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/admin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 400);
            stage.setTitle("Admin");
            stage.setScene(scene);
            stage.show();
            return;
        }

        User user = UserManager.loadUser(username);
        if (user == null){
            lblStatus.setText("User does not exist.");
            return;
        }

        Stage stage = (Stage) btnLogin.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/user.fxml"));
        Parent root = loader.load();

        // Pass user to controller
        UserController controller = loader.getController();
        controller.setUser(user);

        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Photo Album: " + user.getUsername());
        stage.show();
    }

}
