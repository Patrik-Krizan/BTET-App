package org.btet.app;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.btet.util.ScreenManager;

import java.io.IOException;
/**
 * The BtetApp class inherits from the Application class and is the entry point of the application.
 * The application is a desktop application that allows users to manage their tasks.
 * The application is built using the JavaFX framework and the login screen is built using FXML.
 * The application is built using the MVC design pattern.
 * */
public class BtetApp extends Application {
    /**
     * Starts the application by loading the login screen, overriding the start method of the Application class.
     * @param stage The primary stage of the application.
     * */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BtetApp.class.getResource("/fxml/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("BTET Login");
        stage.setScene(scene);
        ScreenManager.setPrimaryStage(stage);
        stage.show();
    }
    /**
     * Calls the launch method of the Application class to start the application on the JavaFX application thread.
     * @param args The command line arguments.
     * */
    public static void main(String[] args) {
        launch();
    }
}