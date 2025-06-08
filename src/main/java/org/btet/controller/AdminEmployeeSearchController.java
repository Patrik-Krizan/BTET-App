package org.btet.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.btet.database_repository.EmployeeRepository;
import org.btet.enums.UserRole;
import org.btet.exception.FormatEmailPhoneException;
import org.btet.exception.RepositoryAccessException;
import org.btet.model.AuditLog;
import org.btet.model.Employee;
import org.btet.util.AuditLoggerUtil;
import org.btet.util.LoginUtil;
import org.btet.util.ValidationUtil;

import java.util.List;
import java.util.logging.Logger;

/**
 * Controller class for AdminEmployeeSearch.fxml, handles the search and update of employees.
 * */
public class AdminEmployeeSearchController {
    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<Employee, String> employeeNameColumn;
    @FXML
    private TableColumn<Employee, String> employeeUsernameColumn;
    @FXML
    private TableColumn<Employee, String> employeeEmailColumn;
    @FXML
    private TableColumn<Employee, String> employeePhoneColumn;
    @FXML
    private TextField nameSearchField;
    @FXML
    private TextField usernameSearchField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private Button updateButton;
    @FXML
    private TextField emailSearchField;
    @FXML
    private TextField phoneSearchField;
    @FXML
    private TextField newPasswordField;
    @FXML
    private TextField confirmPasswordField;

    private EmployeeRepository<Employee> employeeRepository = new EmployeeRepository<>();
    private static Logger logger = Logger.getLogger(AdminEmployeeSearchController.class.getName());
    /**
     * Initializes the table with all employees and sets up the search fields, sets the cell value factories for the columns.
     * Sets up the event handlers for the search fields and the table.
     * */
    public void initialize() {
        employeeNameColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getName()));
        employeeUsernameColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getUsername()));
        employeeEmailColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEmail()));
        employeePhoneColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPhone()));
        employeeTable.getColumns().forEach(column -> column.setStyle("-fx-alignment: CENTER;"));
        loadEmployees();
        nameSearchField.setOnKeyReleased(event -> filterEmployees());
        usernameSearchField.setOnKeyReleased(event -> filterEmployees());
        emailSearchField.setOnKeyReleased(event -> filterEmployees());
        phoneSearchField.setOnKeyReleased(event -> filterEmployees());
        employeeTable.setOnMouseClicked(event -> fillEmployeeDetails());
        updateButton.setOnAction(event -> updateEmployeeDetails());
    }
    /**
     * Loads all employees from the database and sets the table items with the observable list.
     * */
    private void loadEmployees() {
        try{
        List<Employee> employees = employeeRepository.findAll();
        ObservableList<Employee> employeeObservableList = FXCollections.observableList(employees);
        employeeTable.setItems(employeeObservableList);
        }catch (RepositoryAccessException e){
            logger.info("Error loading employees: "+e.getMessage());
        }
    }
    /**
     * Filters the employees based on the search fields and sets the table items with the observable list.
     * */
    private void filterEmployees() {
        String nameSearchText = nameSearchField.getText().toLowerCase();
        String usernameSearchText = usernameSearchField.getText().toLowerCase();
        String emailSearchText = emailSearchField.getText().toLowerCase();
        String phoneSearchText = phoneSearchField.getText().toLowerCase();
        List<Employee> filteredEmployees = employeeRepository.findAll().stream()
                .filter(employee -> employee.getName().toLowerCase().contains(nameSearchText) &&
                        employee.getUsername().toLowerCase().contains(usernameSearchText) &&
                        employee.getEmail().toLowerCase().contains(emailSearchText) &&
                        employee.getPhone().toLowerCase().contains(phoneSearchText))
                .toList();
        ObservableList<Employee> employeeObservableList = FXCollections.observableList(filteredEmployees);
        employeeTable.setItems(employeeObservableList);
    }
    /**
     * Fills the employee details in the text fields when an employee is selected in the table.
     * */
    private void fillEmployeeDetails() {
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            nameField.setText(selectedEmployee.getName());
            usernameField.setText(selectedEmployee.getUsername());
            emailField.setText(selectedEmployee.getEmail());
            phoneField.setText(selectedEmployee.getPhone());
        }
    }
    /**
     * Updates the employee details in the database, if all fields are filled and the email and phone are valid.
     * Alerts the user if there is an error.
     * Updates the employee in the database and updates the username in the login table,
     * calling the {@link LoginUtil#updateUsername(String, String)} method.
     * Adds an audit log for the update.
     * */
    private void updateEmployeeDetails() {
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        try {
            ValidationUtil.validateEmailAndPhone(emailField.getText(), phoneField.getText());
        } catch (FormatEmailPhoneException e) {
            logger.info("Error validating email and phone: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error while updating employee");
            alert.setHeaderText("Invalid email or phone number");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }

        if (selectedEmployee != null) {
            if(employeeRepository.findAll().stream().anyMatch(employee -> employee.getUsername().equals(usernameField.getText()))
                    && !selectedEmployee.getUsername().equals(usernameField.getText())){
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Error while updating employee");
                errorAlert.setContentText("Username already exists!");
                errorAlert.show();
                return;
            }
            else if(nameField.getText().isEmpty() || usernameField.getText().isEmpty() || emailField.getText().isEmpty()
                    || phoneField.getText().isEmpty()){
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error while updating");
                errorAlert.setHeaderText("Error while updating");
                errorAlert.setContentText("All fields must be filled!");
                errorAlert.show();
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Password update Alert");
            alert.setHeaderText("Confirm Update");
            alert.setContentText("Are you sure you want to update the employee details?");

            if (alert.showAndWait().isPresent() && alert.getResult() == ButtonType.OK) {
                String oldUsername = selectedEmployee.getUsername();

                AuditLog auditLog = new AuditLog("Employee - " + oldUsername,
                        "Phone:" + selectedEmployee.getPhone() + "|Email: " + selectedEmployee.getEmail()
                                + "|Username: " + oldUsername + "|Name: " + selectedEmployee.getName(),
                        UserRole.ADMIN.name());

                selectedEmployee.setName(nameField.getText());
                selectedEmployee.setUsername(usernameField.getText());
                selectedEmployee.setEmail(emailField.getText());
                selectedEmployee.setPhone(phoneField.getText());

                employeeRepository.update(selectedEmployee, oldUsername);
                LoginUtil.updateUsername(oldUsername, usernameField.getText());

                auditLog.setNewValue("Phone:" + selectedEmployee.getPhone() + "|Email: " + selectedEmployee.getEmail()
                        + "|Username: " + selectedEmployee.getUsername() + "|Name: " + selectedEmployee.getName());
                AuditLoggerUtil.saveLog(auditLog);
                loadEmployees();
            }
        }
    }
    /**
     * Deletes the selected employee from the database and the login table.
     * Alerts the user to confirm the deletion.
     * Calls the {@link LoginUtil#removeUser(String)} method to remove the user from the login table.
     * */
    public void deleteEmployee(){
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Confirm Delete");
            alert.setContentText("Are you sure you want to delete the employee: \n"+"Name: "+selectedEmployee.getName()
                    +"\nUsername: "+selectedEmployee.getUsername()+"\nEmail: "+selectedEmployee.getEmail()
                    +"\nPhone: "+selectedEmployee.getPhone()
            +"\nThis will include all expenses and budgets associated with this employee." +
                    "\nThis employee won't be able to login to the system. Unless added again.");
            if (alert.showAndWait().isPresent() && alert.getResult() == ButtonType.OK) {
                LoginUtil.removeUser(selectedEmployee.getUsername());
                employeeRepository.delete(selectedEmployee.getUsername());
                loadEmployees();
                nameField.clear();
                usernameField.clear();
                emailField.clear();
                phoneField.clear();
            }
        }
    }
    /**
     * Sets the new password for the selected employee.
     * Alerts the user to confirm the password change.
     * Calls the {@link LoginUtil#updatePassword(String, String)} method to update the password in the login table.
     * */
    public void setNewPassword(){
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            if (newPasswordField.getText().equals(confirmPasswordField.getText())) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Alert");
                alert.setHeaderText("Confirm Password Change");
                alert.setContentText("Are you sure you want to change the password for the employee: \n" + "Name: " + selectedEmployee.getName()
                        + "\nUsername: " + selectedEmployee.getUsername()+"\nThis should be done only if the employee requested the change." +
                        "\nThis change will be logged");
                if (alert.showAndWait().isPresent() && alert.getResult() == ButtonType.OK) {
                    AuditLog auditLog = new AuditLog("Employee - " + selectedEmployee.getUsername(),
                            LoginUtil.getUserPasswordMap().get(selectedEmployee.getUsername()), UserRole.ADMIN.name());
                    LoginUtil.updatePassword(selectedEmployee.getUsername(), newPasswordField.getText());
                    auditLog.setNewValue(LoginUtil.getUserPasswordMap().get(selectedEmployee.getUsername()));
                    AuditLoggerUtil.saveLog(auditLog);
                    loadEmployees();
                    confirmPasswordField.clear();
                    newPasswordField.clear();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Password Mismatch");
                alert.setContentText("New password and confirm password do not match.");
                alert.showAndWait();
            }
        }
    }
}