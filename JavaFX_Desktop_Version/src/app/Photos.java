package app;
import java.io.IOException;

import app.util.UserManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import view.controller.LoginController;

/**
 * Main class for the Photo Album application.
 * Launches the JavaFX UI and initializes the stock user if needed.
 * This is the entry point for the application.
 * 
 * @author Toma Takamatsu
 */
public class Photos extends Application {

    /**
     * Sets up and displays the login screen.
     *
     * @param stage The primary stage for the application.
     * @throws Exception if the FXML loading fails.
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();

        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("Login Menu");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        UserManager.initializeStockUserIfNeeded();
        launch(args);
    }
}