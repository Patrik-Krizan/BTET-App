package org.btet.controller;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.btet.database_repository.ManagerRepository;
import org.btet.enums.UserRole;
import org.btet.exception.RepositoryAccessException;
import org.btet.model.AuditLog;
import org.btet.model.Manager;
import org.btet.util.AuditLoggerUtil;
import org.btet.util.LoginUtil;

import java.util.List;
import java.util.logging.Logger;

/**
 * This class is the controller for the AdminManagerSearch.fxml file.
 * It is responsible for handling the search and update of manager details.
 * It also handles the deletion of managers and changing their passwords.
 */
public class AdminManagerSearchController {
    @FXML
    private TableView<Manager> managerTable;
    @FXML
    private TableColumn<Manager, String> nameColumn;
    @FXML
    private TableColumn<Manager, String> usernameColumn;
    @FXML
    private TableColumn<Manager, String> departmentColumn;
    @FXML
    private TextField nameSearchField;
    @FXML
    private TextField usernameSearchField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField departmentField;
    @FXML
    private Button updateButton;
    @FXML
    private TextField departmentSearchField;
    @FXML
    private TextField newPasswordField;
    @FXML
    private TextField confirmPasswordField;
    private static Logger logger = Logger.getLogger(AdminManagerSearchController.class.getName());
    private ManagerRepository<Manager> managerRepository = new ManagerRepository<>();
    /**
     * Initializes the table view with the manager details and search fields, sets cell value factories for the table columns.
     * Sets up the event handlers for the search fields, table view and the update button.
     */
    public void initialize() {
        nameColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getName()));
        usernameColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getUsername()));
        departmentColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDepartment()));
        managerTable.getColumns().forEach(column -> column.setStyle("-fx-alignment: CENTER;"));
        loadManagers();
        nameSearchField.setOnKeyReleased(event -> filterManagers());
        usernameSearchField.setOnKeyReleased(event -> filterManagers());
        departmentSearchField.setOnKeyReleased(event -> filterManagers());
        managerTable.setOnMouseClicked(event -> fillManagerDetails());
        updateButton.setOnAction(event -> updateManagerDetails());
    }
    /**
     * Loads the managers from the database and sets the table view with the manager details.
     */
    private void loadManagers() {
        try {
            List<Manager> managers = managerRepository.findAll();
            ObservableList<Manager> managerObservableList = FXCollections.observableList(managers);
            managerTable.setItems(managerObservableList);
        }catch (RepositoryAccessException e){
            logger.info("Error loading managers from the database: " + e.getMessage());
        }
    }
    /**
     * Filters the managers based on the search text inputted in the search fields.
     */
    private void filterManagers() {
        String nameSearchText = nameSearchField.getText().toLowerCase();
        String usernameSearchText = usernameSearchField.getText().toLowerCase();
        String departmentSearchText = departmentSearchField.getText().toLowerCase();
        List<Manager> filteredManagers = managerRepository.findAll().stream()
                .filter(manager -> manager.getName().toLowerCase().contains(nameSearchText) &&
                        manager.getUsername().toLowerCase().contains(usernameSearchText) &&
                        manager.getDepartment().toLowerCase().contains(departmentSearchText))
                .toList();
        ObservableList<Manager> managerObservableList = FXCollections.observableList(filteredManagers);
        managerTable.setItems(managerObservableList);
    }
    /**
     * Fills the manager details in the text fields when a manager is selected in the table view.
     */
    private void fillManagerDetails() {
        Manager selectedManager = managerTable.getSelectionModel().getSelectedItem();
        if (selectedManager != null) {
            nameField.setText(selectedManager.getName());
            usernameField.setText(selectedManager.getUsername());
            departmentField.setText(selectedManager.getDepartment());
        }
    }
    /**
     * Updates the manager details in the database and the login details when the update button is clicked.
     * Alerts the user to confirm the update before proceeding.
     * Updates the Manager in the database and the login details for the manager.
     * Calling the {@link ManagerRepository#update(Manager, String)} method to update the manager details in the database.
     * Logs the update in the audit log.
     */
    private void updateManagerDetails() {
        Manager selectedManager = managerTable.getSelectionModel().getSelectedItem();
        if (selectedManager != null) {
            if(managerRepository.findAll().stream().anyMatch(manager -> manager.getUsername().equals(usernameField.getText()))
                    && !selectedManager.getUsername().equals(usernameField.getText())){
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Error while updating manager");
                errorAlert.setContentText("Username already exists!");
                errorAlert.show();
                return;
            }else if(nameField.getText().isEmpty() || usernameField.getText().isEmpty() || departmentField.getText().isEmpty()){
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error while updating");
                errorAlert.setHeaderText("Error while updating");
                errorAlert.setContentText("All fields must be filled!");
                errorAlert.show();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Confirm Update");
            alert.setContentText("Are you sure you want to update the manager details?");

            if (alert.showAndWait().isPresent() && alert.getResult() == ButtonType.OK) {
                String oldUsername = selectedManager.getUsername();

                AuditLog auditLog = new AuditLog("Manager - " + oldUsername,
                        "Department:" + selectedManager.getDepartment() + "|Username: " + oldUsername + "|Name: " + selectedManager.getName(),
                        UserRole.ADMIN.name());

                selectedManager.setName(nameField.getText());
                selectedManager.setUsername(usernameField.getText());
                selectedManager.setDepartment(departmentField.getText());

                managerRepository.update(selectedManager, oldUsername);
                LoginUtil.updateUsername(oldUsername, usernameField.getText());

                auditLog.setNewValue("Department:" + selectedManager.getDepartment() + "|Username: " + selectedManager.getUsername() + "|Name: " + selectedManager.getName());
                AuditLoggerUtil.saveLog(auditLog);
                loadManagers();
            }
        }
    }
    /**
     * Deletes the manager from the database and the login details when the delete button is clicked.
     * Alerts the user to confirm the deletion before proceeding.
     * Deletes the Manager from the database and the login details for the manager.
     * Calling the {@link ManagerRepository#delete(String)} method to delete the manager from the database.
     */
    public void deleteManager() {
        Manager selectedManager = managerTable.getSelectionModel().getSelectedItem();
        if (selectedManager != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Confirm Delete");
            alert.setContentText("Are you sure you want to delete the manager: \n" + "Name: " + selectedManager.getName()
                    + "\nUsername: " + selectedManager.getUsername() + "\nDepartment: " + selectedManager.getDepartment()
                    +"\nThis will include deleting all budgets this manager has provided." +
                    "\nThis manager won't be able to login to the system. Unless added again.");
            if (alert.showAndWait().isPresent() && alert.getResult() == ButtonType.OK) {
                LoginUtil.removeUser(selectedManager.getUsername());
                managerRepository.delete(selectedManager.getUsername());
                loadManagers();
                nameField.clear();
                usernameField.clear();
                departmentField.clear();
            }
        }
    }
    /**
     * Sets the new password for the manager when the set password button is clicked, calling the
     * {@link LoginUtil#updatePassword(String, String)} method.
     * */
    public void setNewPassword(){
        Manager selectedManager = managerTable.getSelectionModel().getSelectedItem();
        if (selectedManager != null) {
            if (newPasswordField.getText().equals(confirmPasswordField.getText())) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Alert changing password");
                alert.setHeaderText("Confirm Password Change");
                alert.setContentText("Are you sure you want to change the password for the manager: \n" + "Name: " + selectedManager.getName()
                        + "\nUsername: " + selectedManager.getUsername() + "\nDepartment: " + selectedManager.getDepartment()
                        +"\nThis should be done only if the manager requested the change." +
                        "\nThis change will be logged");
                if (alert.showAndWait().isPresent() && alert.getResult() == ButtonType.OK) {
                    AuditLog auditLog = new AuditLog("Manager - " + selectedManager.getUsername(),
                            LoginUtil.getUserPasswordMap().get(selectedManager.getUsername()), UserRole.ADMIN.name());
                    LoginUtil.updatePassword(selectedManager.getUsername(), newPasswordField.getText());
                    auditLog.setNewValue(LoginUtil.getUserPasswordMap().get(selectedManager.getUsername()));
                    AuditLoggerUtil.saveLog(auditLog);
                    loadManagers();
                    confirmPasswordField.clear();
                    newPasswordField.clear();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Password Mismatch");
                alert.setContentText("New password and confirm password do not match. Please try again.");
                alert.showAndWait();
            }
        }
    }
}