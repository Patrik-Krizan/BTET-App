package org.btet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.btet.enums.UserRole;
import org.btet.exception.InvalidLoginCredentialsException;
import org.btet.model.UserLoginRecord;
import org.btet.util.LoginUtil;
import org.btet.util.ScreenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Controller class for the Login Page, which is the first page that the user sees when they open the application.
 * This page allows the user to log in to the application using their username and password.
 */
public class LoginController {
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField usernameField;
    @FXML
    private Label errorLabel;
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    /**
     * Logs the user in to the application using the username and password inputted in the usernameField and passwordField.
     * Redirects the user to the appropriate home screen based on their role
     * Displays an error message if the username or password is incorrect.
     */
    public void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        try {
            UserLoginRecord userLoginRecord = LoginUtil.login(username, password);
            UserRole userRole = userLoginRecord.role();
            switch (userRole) {
                case EMPLOYEE:
                    ScreenManager.showScreen("/fxml/EmployeeHomeScreen.fxml");
                    break;
                case MANAGER:
                    ScreenManager.showScreen("/fxml/ManagerHomeScreen.fxml");
                    break;
                case ADMIN:
                    ScreenManager.showScreen("/fxml/AdminHome.fxml");
                    break;
            }
        } catch (InvalidLoginCredentialsException e) {
            logger.info("Invalid username or password entered: username:{}|password:{}", username, password);
            errorLabel.setText(e.getMessage());
        }
    }
}