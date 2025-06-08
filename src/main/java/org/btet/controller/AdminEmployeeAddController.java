package org.btet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.btet.database_repository.EmployeeRepository;
import org.btet.enums.UserRole;
import org.btet.exception.FormatEmailPhoneException;
import org.btet.model.Employee;
import org.btet.util.LoginUtil;
import org.btet.util.ValidationUtil;

import java.util.logging.Logger;

/**
 * Controller class for AdminEmployeeAdd.fxml, handles the addition of employees.
 * */
public class AdminEmployeeAddController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField passwordField;

    private static EmployeeRepository<Employee> employeeRepository = new EmployeeRepository<>();
    private static Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
    private static Alert errorAlert = new Alert(Alert.AlertType.ERROR);
    private static Logger logger = Logger.getLogger(AdminEmployeeAddController.class.getName());
    /**
     * Adds an employee to the database, if all fields are filled and the username is unique.
     * Alerts the user if there is an error.
     * Adds the employee to the database and creates a new user in the login table.
     */
    public void addEmployee(){
        String name = nameField.getText();
        String username = usernameField.getText();
        try{
            ValidationUtil.validateEmailAndPhone(emailField.getText(), phoneField.getText());
        }catch (FormatEmailPhoneException e){
            logger.info("Error while adding employee: "+e.getMessage());
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Error while adding employee");
            errorAlert.setContentText(e.getMessage());
            errorAlert.show();
            return;
        }
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        if(name.isEmpty() || username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()){
            errorAlert.setContentText("All fields must be filled!");
            errorAlert.show();
            return;
        }
        if(employeeRepository.findAll().stream().anyMatch(employee -> employee.getUsername().equals(username))){
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Error while adding employee");
            errorAlert.setContentText("Username already exists!");
            errorAlert.show();
            return;
        }
        Employee employee = new Employee(name, username, email, phone);
        confirmationAlert.setTitle("Alert");
        confirmationAlert.setHeaderText("Confirm Adding");
        confirmationAlert.setContentText("Are you sure you want to add this employee: \n"+"Name: "+employee.getName()
                +"\nUsername: "+employee.getUsername()+"\nEmail: "+employee.getEmail()
                +"\nPhone: "+employee.getPhone());
        if(confirmationAlert.showAndWait().isPresent() && confirmationAlert.getResult() == ButtonType.OK){
            employeeRepository.save(employee);
            LoginUtil.addNewUser(username, password, UserRole.EMPLOYEE);
            nameField.clear();
            usernameField.clear();
            emailField.clear();
            phoneField.clear();
            passwordField.clear();
        }
    }
}
