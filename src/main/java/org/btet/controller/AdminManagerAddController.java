package org.btet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.btet.database_repository.ManagerRepository;
import org.btet.enums.UserRole;
import org.btet.exception.RepositoryAccessException;
import org.btet.model.Manager;
import org.btet.util.LoginUtil;

import java.util.logging.Logger;

/**
 * Controller class for the add manager page, which is used by the admin to add a new manager to the system.
 * */
public class AdminManagerAddController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField departmentField;
    @FXML
    private TextField passwordField;

    private static ManagerRepository<Manager> managerRepository = new ManagerRepository<>();
    private static final Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
    private static final Alert errorAlert = new Alert(Alert.AlertType.ERROR);
    /**
     * Adds a manager to the database, if all fields are filled and the username is unique.
     * Alerts the user if there is an error.
     * Adds the manager to the database and creates a new user in the login table.
     * */
    public void addManager() {
        String name = nameField.getText();
        String username = usernameField.getText();
        String department = departmentField.getText();
        String password = passwordField.getText();

        if (name.isEmpty() || username.isEmpty() || department.isEmpty() || password.isEmpty()) {
            errorAlert.setContentText("All fields must be filled!");
            errorAlert.show();
            return;
        }

        if (managerRepository.findAll().stream().anyMatch(manager -> manager.getUsername().equals(username))) {
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Error while adding manager");
            errorAlert.setContentText("Username already exists!");
            errorAlert.show();
            return;
        }

        Manager manager = new Manager(name, username, department);
        confirmationAlert.setTitle("Alert");
        confirmationAlert.setHeaderText("Confirm Adding");
        confirmationAlert.setContentText("Are you sure you want to add this manager: \n" + "Name: " + manager.getName()
                + "\nUsername: " + manager.getUsername() + "\nDepartment: " + manager.getDepartment());
        if (confirmationAlert.showAndWait().isPresent() && confirmationAlert.getResult() == ButtonType.OK) {
            try{
            managerRepository.save(manager);
            }catch (RepositoryAccessException e){
                Logger.getLogger(AdminManagerAddController.class.getName()).info("Error saving to manager repository: "+e.getMessage());
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Error while adding manager");
                errorAlert.setContentText("Username already exists!");
                errorAlert.show();
                return;
            }
            LoginUtil.addNewUser(username, password, UserRole.MANAGER);
            nameField.clear();
            usernameField.clear();
            departmentField.clear();
            passwordField.clear();
        }
    }
}