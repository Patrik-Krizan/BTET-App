package org.btet.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * ScreenManager class is used to manage the screens in the application,
 * by providing static methods for showing screens on the primary stage, and for showing popups on new stages.
 * */
public class ScreenManager {
    private static Logger logger = Logger.getLogger(ScreenManager.class.getName());
    private static Stage primaryStage;
    /**
     * Sets the primary stage of the application, so that it can be used to show new screens(switch screens).
     * @param stage the primary stage of the application
     * */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }
    /**
     * Shows a new screen on the primary stage of the application.
     * @param fxmlFile the path to the FXML file of the screen to be shown
     * */
    public static void showScreen(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(ScreenManager.class.getResource(fxmlFile));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.centerOnScreen();
            primaryStage.show();
            primaryStage.setTitle("BTET App");
        } catch (IOException e) {
            logger.info("Error loading screen: "+e.getMessage());
        }
    }
    /**
     * Shows a popup screen on a new stage.
     * @param fxmlFile the path to the FXML file of the popup screen to be shown
     * */
    public static void popupScreen(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(ScreenManager.class.getResource(fxmlFile));
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.setScene(new Scene(root));
            popupStage.setTitle(LoginUtil.getLoggedUserRecord().username()+"- BTET");
            popupStage.show();
        } catch (IOException e) {
            logger.info("Error loading popup screen: "+e.getMessage());
        }
    }
    private ScreenManager(){}
}
