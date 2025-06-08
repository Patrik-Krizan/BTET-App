package org.btet.controller;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.btet.database_repository.*;
import org.btet.enums.ExpenseStatus;
import org.btet.enums.UserRole;
import org.btet.model.*;
import org.btet.util.AuditLoggerUtil;
import org.btet.util.LoginUtil;
import org.btet.util.ScreenManager;
import org.btet.util.ValidationUtil;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
/**
 * Controller class for the Manager Home Page, which is the first page that the manager sees after logging in.
 * This page displays the pending expenses for managers to approve/reject, along with employees and their budget
 * information. Managers can also allocate budgets to employees and view receipts of the expenses.
 */
public class ManagerHomeController {
    @FXML private Label welcomeLabel;
    @FXML private TableView<Expense> pendingExpensesTable;
    @FXML private TableColumn<Expense, String> pendingExpenseIDColumn;
    @FXML private TableColumn<Expense, String> pendingExpenseByColumn;
    @FXML private TableColumn<Expense, String> pendingExpenseTypeColumn;
    @FXML private TableColumn<Expense, String> pendingExpenseAmountColumn;
    @FXML private TableColumn<Expense, String> pendingExpenseDateColumn;
    @FXML private TableColumn<Expense, String> pendingExpenseStatusColumn;
    @FXML private Button filterExpenseStatusButton;
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> employeeNameColumn;
    @FXML private TableColumn<Employee, String> employeeUsernameColumn;
    @FXML private TableColumn<Employee, String> employeeEmailColumn;
    @FXML private TableColumn<Employee, String> employeePhoneColumn;
    @FXML private Label allocatedAmountLabel;
    @FXML private Label spentAmountLabel;
    @FXML private TextField allocateBudgetField;
    @FXML private Label budgetTitleLabel;

    private  UserLoginRecord userLoginRecord = LoginUtil.getLoggedUserRecord();
    private static Alert errorAlert = new Alert(Alert.AlertType.ERROR);
    private static Alert informationAlert = new Alert(Alert.AlertType.INFORMATION);
    private Boolean viewAllExpenses = false;
    private static final String PENDING_STRING = "PENDING";
    private static final String ERROR_STRING = "Error";
    ExpenseRepository expenseRepository = new ExpenseRepository();
    EmployeeRepository<Employee> employeeRepository = new EmployeeRepository<>();
    BudgetRepository<Budget> budgetRepository = new BudgetRepository<>();
    /**
     * Initializes the Manager Home Page, sets cell values for the pending expenses table and employee table,
     * loads the pending expenses and employees; sets up a listener for the filterExpenseStatusButton to toggle
     * between viewing all expenses and viewing only pending expenses, and sets up a listener for the employeeTable
     * to handle employee clicks.
     * This method is called automatically when the FXML file is loaded.
     */
    public void initialize(){
        welcomeLabel.setText(userLoginRecord.username() + "|" + userLoginRecord.role());
        pendingExpenseIDColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId().toString()));
        pendingExpenseByColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getBy()));
        pendingExpenseTypeColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getExpenseType().name()));
        pendingExpenseAmountColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmount().toString()));
        pendingExpenseDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate().toString()));
        pendingExpenseStatusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStatus().name()));
        pendingExpensesTable.getColumns().forEach(column -> column.setStyle("-fx-alignment: CENTER;"));
        employeeNameColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getName()));
        employeeUsernameColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getUsername()));
        employeeEmailColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEmail()));
        employeePhoneColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPhone()));
        employeeTable.getColumns().forEach(column -> column.setStyle("-fx-alignment: CENTER;"));
        filterExpenseStatusButton.setOnAction(event -> {
            viewAllExpenses = !viewAllExpenses;
            filterExpenseStatusButton.setText("VIEW " + (Boolean.TRUE.equals(viewAllExpenses) ? PENDING_STRING : "ALL"));
            loadPendingExpenses();
        });
        loadPendingExpenses();
        loadEmployees();
        employeeTable.setOnMouseClicked(event -> handleEmployeeClick());
    }
    /**
     * Loads the employees from the database and displays them in the employee table.
     */
    private void loadEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        ObservableList<Employee> employeeObservableList = FXCollections.observableList(employees);
        employeeTable.setItems(employeeObservableList);
    }
    /**
     * Handles the employee click event on the Employee table by displaying the budget information of the selected employee,
     * including the allocated amount and spent amount.
     */
    private void handleEmployeeClick() {
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            budgetTitleLabel.setText(selectedEmployee.getName() + "s Budget Info");
            Budget budget = budgetRepository.findByUsername(selectedEmployee.getUsername());
            if (budget != null) {
                budgetRepository.updateSpentAmount(selectedEmployee.getUsername());
                allocatedAmountLabel.setText(budget.getAllocatedAmount().toString());
                spentAmountLabel.setText(budget.getSpentAmount().toString());
            }
            else{
                allocatedAmountLabel.setText("0");
                spentAmountLabel.setText("0");
            }
        }
    }
    /**
     * Loads the pending expenses from the database and displays them in the pending expenses table, which can
     * be filtered to show all expenses or only pending expenses.
     */
    private void loadPendingExpenses(){
        List<Expense> expenses = expenseRepository.findAll();
        if(Boolean.FALSE.equals(viewAllExpenses)){
            expenses = expenses.stream().filter(expense -> expense.getStatus().name().equals(PENDING_STRING)).toList();
        }
        ObservableList<Expense> pendingExpenseObservableList = FXCollections.observableList(expenses);
        pendingExpensesTable.setItems(pendingExpenseObservableList);
    }
    /**
     * Approves the selected expense in the pending expenses table by updating its status to APPROVED in the database.
     * Logs the change in the audit log.
     */
    public void approveExpense(){
        Expense selectedExpense = pendingExpensesTable.getSelectionModel().getSelectedItem();
        if(selectedExpense != null){
            AuditLog auditLog = new AuditLog("Expense Status - expense of "+selectedExpense.getBy(),
                    selectedExpense.getStatus().name(), UserRole.MANAGER.name());
            expenseRepository.updateStatus(selectedExpense.getId(), ExpenseStatus.APPROVED);
            auditLog.setNewValue(ExpenseStatus.APPROVED.name());
            AuditLoggerUtil.saveLog(auditLog);
            loadPendingExpenses();
            informationAlert.setTitle("Alert");
            informationAlert.setHeaderText("Expense approved successfully");
            informationAlert.setContentText("You have successfully approved the expense.");
            informationAlert.showAndWait();
        }
    }
    /**
     * Rejects the selected expense in the pending expenses table by updating its status to REJECTED in the database.
     * Logs the change in the audit log.
     */
    public void rejectExpense(){
        Expense selectedExpense = pendingExpensesTable.getSelectionModel().getSelectedItem();
        if(selectedExpense != null){
            AuditLog auditLog = new AuditLog("Expense Status", selectedExpense.getStatus().name(), UserRole.MANAGER.name());
            expenseRepository.updateStatus(selectedExpense.getId(), ExpenseStatus.REJECTED);
            auditLog.setNewValue(ExpenseStatus.APPROVED.name());
            AuditLoggerUtil.saveLog(auditLog);
            loadPendingExpenses();
            informationAlert.setTitle("Alert");
            informationAlert.setHeaderText("Expense rejected successfully");
            informationAlert.setContentText("You have successfully rejected the expense.");
            informationAlert.showAndWait();
        }
    }
    /**
     * Opens the receipt file of the selected expense in the pending expenses table, using the Desktop class from
     * the java.awt package.
     */
    public void viewReceipt(){
        Expense selectedExpense = pendingExpensesTable.getSelectionModel().getSelectedItem();
        if (selectedExpense != null && selectedExpense.getReceiptFilePath() != null) {
            File receiptFile = new File(selectedExpense.getReceiptFilePath());
            if (receiptFile.exists()) {
                try {
                    Desktop.getDesktop().open(receiptFile);
                } catch (IOException e) {
                    errorAlert.setTitle(ERROR_STRING);
                    errorAlert.setHeaderText("Error opening receipt file");
                    errorAlert.setContentText("An error occurred while trying to open the receipt file.");
                }
            } else {
                errorAlert.setTitle(ERROR_STRING);
                errorAlert.setHeaderText("Receipt file not found");
                errorAlert.setContentText("The receipt file for the selected expense was not found.");
            }
        } else {
            errorAlert.setTitle(ERROR_STRING);
            errorAlert.setHeaderText("No receipt file");
            errorAlert.setContentText("The selected expense does not have a receipt file.");
        }
    }
    /**
     * Allocates the budget amount inputted in the allocateBudgetField to the selected employee in the employee table.
     * If the employee already has a budget, the allocated amount is updated. Logs the change in the audit log.
     */
    public void allocateBudget(){
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        if(selectedEmployee != null){
            String allocatedAmountString = allocateBudgetField.getText();
            BigDecimal allocatedAmount = ValidationUtil.parseBigDecimal(allocatedAmountString);
            if(allocatedAmount == null || allocatedAmount.compareTo(BigDecimal.valueOf(1000000)) >= 1
                    || allocatedAmount.compareTo(BigDecimal.ZERO) < 0){
                errorAlert.setTitle(ERROR_STRING);
                errorAlert.setHeaderText("Wrong amount entered!");
                errorAlert.setContentText("Please enter an amount in bounds of [0, 1 000 000]");
                errorAlert.showAndWait();
                return;
            }
            Budget budget = budgetRepository.findByUsername(selectedEmployee.getUsername());
            if(budget == null){
                budget = new Budget.Builder().allocateBudgetAmount(allocatedAmount).givenTo(selectedEmployee.getUsername())
                        .givenBy(userLoginRecord.username()).build();
                budgetRepository.save(budget);
                allocateBudgetField.clear();
            }
            else{
                AuditLog auditLog = new AuditLog("Budget Allocated Amount for "+selectedEmployee.getUsername()
                        , budget.getAllocatedAmount().toString(),
                        UserRole.MANAGER.name());
                budgetRepository.updateAllocatedAmount(selectedEmployee.getUsername(), allocatedAmount);
                auditLog.setNewValue(budgetRepository.findByUsername(selectedEmployee.getUsername()).getAllocatedAmount().toString());
                AuditLoggerUtil.saveLog(auditLog);
                allocateBudgetField.clear();
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Budget Allocation");
            alert.setHeaderText("Budget allocated successfully");
            alert.setContentText("The amount of " + allocatedAmount + " has been allocated to " + selectedEmployee.getUsername());
            alert.showAndWait();
            loadEmployees();
        }
    }
    /**
     * Deletes the budget of the selected employee in the employee table, if the employee has a budget.
     */
    public void deleteBudget() {
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            Budget selectedBudget = budgetRepository.findByUsername(selectedEmployee.getUsername());
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this budget?");
            confirmationAlert.setContentText("Budget amount: " + selectedBudget
                    .getAllocatedAmount()+"\nSpent amount: "+selectedBudget
                    .getSpentAmount()+"\nGiven to: "+selectedEmployee.getUsername()+"\n"+
                    "Given by: "+selectedBudget.getGivenBy());
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    budgetRepository.deleteByUsername(selectedEmployee.getUsername());
                    allocatedAmountLabel.setText("");
                    spentAmountLabel.setText("");
                    budgetTitleLabel.setText("");
                }
            });
        }
    }
    /**
     * Refreshes the pending expenses table by loading the pending expenses from the database.
     */
    public void refreshExpenseTable(){
        loadPendingExpenses();
    }
    /**
     * Logs out the user and returns to the login screen.
     */
    public void logOut() {
        LoginUtil.logout();
        ScreenManager.showScreen("/fxml/Login.fxml");
    }
    /**
     * Exits the application.
     */
    public void exit() {
        System.exit(0);
    }
}

