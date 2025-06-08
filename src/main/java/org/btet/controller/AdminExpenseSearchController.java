package org.btet.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import org.btet.database_repository.ExpenseRepository;
import org.btet.enums.ExpenseStatus;
import org.btet.enums.ExpenseType;
import org.btet.enums.UserRole;
import org.btet.exception.RepositoryAccessException;
import org.btet.model.AuditLog;
import org.btet.model.Expense;
import org.btet.util.AuditLoggerUtil;
import org.btet.util.ValidationUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
/**
 * Controller class for AdminExpenseSearch.fxml, handles the search and update of expenses.
 * */
public class AdminExpenseSearchController {
    @FXML private TableView<Expense> expenseTableView;
    @FXML private TableColumn<Expense, String> idTableColumn;
    @FXML private TableColumn<Expense, String> byTableColumn;
    @FXML private TableColumn<Expense, String> expenseTypeTableColumn;
    @FXML private TableColumn<Expense, String> amountTableColumn;
    @FXML private TableColumn<Expense, String> dateTableColumn;
    @FXML private TableColumn<Expense, String> statusTableColumn;
    @FXML private TextField filterIdField;
    @FXML private ComboBox<String> filterExpenseTypeComboBox;
    @FXML private TextField fromAmountFilterField;
    @FXML private TextField toAmountFilterField;
    @FXML private DatePicker fromDateFilterField;
    @FXML private DatePicker toDateFilterField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> expenseTypeUpdateComboBox;
    @FXML private TextField amountUpdateField;

    private static Logger logger = Logger.getLogger(AdminExpenseSearchController.class.getName());
    private static Alert errorAlert = new Alert(Alert.AlertType.ERROR);
    private static final String ERROR_STRING = "Error";
    private static final String NO_CHOICE_STRING = "<no choice>";
    private ExpenseRepository expenseRepository = new ExpenseRepository();
    /**
     * Initializes the expense type, and the filter expense type update combo box with the expense types.
     * Initializes the status filter combo box with the expense statuses.
     * Initializes the expense table view with the expenses.
     * Sets the cell value factories for the table columns, and loads the expenses.
     * Sets the event handler for the table view to fill the update fields.
     * */
    public void initialize() {
        expenseTypeUpdateComboBox.getItems().add(NO_CHOICE_STRING);
        expenseTypeUpdateComboBox.getItems().addAll(Stream.of(ExpenseType.values()).map(Enum::name).toList());
        expenseTypeUpdateComboBox.getSelectionModel().select(0);

        filterExpenseTypeComboBox.getItems().add(NO_CHOICE_STRING);
        filterExpenseTypeComboBox.getItems().addAll(Stream.of(ExpenseType.values()).map(Enum::name).toList());
        filterExpenseTypeComboBox.getSelectionModel().select(0);

        statusFilterComboBox.getItems().add(NO_CHOICE_STRING);
        statusFilterComboBox.getItems().addAll(Stream.of(ExpenseStatus.values()).map(Enum::name).toList());
        statusFilterComboBox.getSelectionModel().select(0);
        idTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId().toString()));
        byTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getBy()));
        expenseTypeTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getExpenseType().name()));
        amountTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmount().toString()));
        dateTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate().toString()));
        statusTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStatus().name()));
        expenseTableView.getColumns().forEach(c -> c.setStyle("-fx-alignment: CENTER;"));
        expenseTableView.setOnMouseClicked(event -> fillUpdateFields());

        loadExpenses();
    }
    /**
     * Fills the update fields with the selected expense's details through event handling.
     * */
    private void fillUpdateFields() {
        Expense selectedExpense = expenseTableView.getSelectionModel().getSelectedItem();
        if (selectedExpense != null) {
            expenseTypeUpdateComboBox.setValue(selectedExpense.getExpenseType().name());
            amountUpdateField.setText(selectedExpense.getAmount().toString());
        }
    }
    /**
     * Loads the expenses from the database and sets the table view items.
     * */
    private void loadExpenses() {
        try {
            List<Expense> expenses = expenseRepository.findAll();
            ObservableList<Expense> expenseObservableList = FXCollections.observableList(expenses);
            expenseTableView.setItems(expenseObservableList);
        }catch (RepositoryAccessException e){
            logger.info("Error loading expenses: "+e.getMessage());
        }
    }
    /**
     * Filters the expenses based on the filter fields and sets the table view items.
     * */
    public void filterExpenses() {
        List<Expense> allExpenses = expenseRepository.findAll();
        String idFilterText = filterIdField.getText().trim();
        String expenseTypeFilter = filterExpenseTypeComboBox.getValue();
        String fromAmountText = fromAmountFilterField.getText().trim();
        String toAmountText = toAmountFilterField.getText().trim();
        BigDecimal fromAmount = ValidationUtil.parseBigDecimal(fromAmountText);
        BigDecimal toAmount = ValidationUtil.parseBigDecimal(toAmountText.trim());
        LocalDate fromDate = fromDateFilterField.getValue();
        LocalDate toDate = toDateFilterField.getValue();
        String statusFilter = statusFilterComboBox.getValue();
        List<Expense> filteredExpenses = allExpenses.stream()
                .filter(expense -> idFilterText.isEmpty() || String.valueOf(expense.getId()).contains(idFilterText))
                .filter(expense -> expenseTypeFilter.equals(NO_CHOICE_STRING) || expense.getExpenseType().name().equals(expenseTypeFilter))
                .filter(expense -> statusFilter.equals(NO_CHOICE_STRING) || expense.getStatus().name().equals(statusFilter))
                .filter(expense -> fromAmount == null || expense.getAmount().compareTo(fromAmount) >= 0)
                .filter(expense -> toAmount == null || expense.getAmount().compareTo(toAmount) <= 0)
                .filter(expense -> fromDate == null || expense.getDate().isAfter(fromDate))
                .filter(expense -> toDate == null || expense.getDate().isBefore(toDate)).toList();
        expenseTableView.setItems(FXCollections.observableList(filteredExpenses));
    }
    /**
     * Opens the receipt file of the selected expense, using the Desktop class from java.awt.
     * Shows an error alert if the file is not found or an error occurs while opening the file.
     * */
    public void viewReceipt() {
        Expense selectedExpense = expenseTableView.getSelectionModel().getSelectedItem();
        if (selectedExpense != null && selectedExpense.getReceiptFilePath() != null) {
            File receiptFile = new File(selectedExpense.getReceiptFilePath());
            if (receiptFile.exists()) {
                try {
                    Desktop.getDesktop().open(receiptFile);
                } catch (IOException e) {
                    logger.severe("Error opening receipt file: " + e.getMessage());
                    errorAlert.setTitle(ERROR_STRING);
                    errorAlert.setHeaderText("Error opening receipt file");
                    errorAlert.setContentText("An error occurred while trying to open the receipt file.");
                    errorAlert.show();
                }
            } else {
                errorAlert.setTitle(ERROR_STRING);
                errorAlert.setHeaderText("Receipt file not found");
                errorAlert.setContentText("The receipt file for the selected expense was not found.");
                errorAlert.show();
            }
        } else {
            errorAlert.setTitle(ERROR_STRING);
            errorAlert.setHeaderText("No receipt file");
            errorAlert.setContentText("The selected expense does not have a receipt file.");
            errorAlert.show();
        }
    }
    /**
     * Deletes the selected expense from the database.
     * Shows a confirmation alert before deleting the expense.
     * */
    public void deleteExpense() {
        Expense selectedExpense = expenseTableView.getSelectionModel().getSelectedItem();
        if (selectedExpense != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete expense");
            alert.setHeaderText("Are you sure you want to delete this expense?");
            alert.setContentText("Expense type: " + selectedExpense.getExpenseType() + "\nAmount: " + selectedExpense.getAmount() + "€");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    expenseRepository.deleteById(selectedExpense.getId());
                    loadExpenses();
                    expenseTypeUpdateComboBox.getSelectionModel().select(0);
                    amountUpdateField.clear();
                }
            });
        } else {
            errorAlert.setTitle(ERROR_STRING);
            errorAlert.setHeaderText("No expense selected");
            errorAlert.setContentText("Please select an expense to delete.");
            errorAlert.show();
        }
    }
    /**
     * Updates the selected expense with the new details from the update fields.
     * Shows an error alert if the input is invalid, and a confirmation alert before updating the expense.
     * Adds an audit log for the update.
     * */
    public void updateExpense() {
        Expense selectedExpense = expenseTableView.getSelectionModel().getSelectedItem();
        if (selectedExpense != null) {
            String expenseTypeString = expenseTypeUpdateComboBox.getValue();
            String amountString = amountUpdateField.getText();
            StringBuilder sb = new StringBuilder();
            ExpenseType expenseType = null;
            if (expenseTypeString.equals(NO_CHOICE_STRING)) {
                sb.append("Expense type cannot be unselected\n");
            }
            else expenseType = ExpenseType.valueOf(expenseTypeString);

            BigDecimal amount = ValidationUtil.parseBigDecimal(amountString);
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(new BigDecimal(100000)) >= 0) {
                sb.append("Amount cannot be empty, and it must be in bounds [0, 100 000].\n");
            }
            if (!sb.isEmpty()) {
                errorAlert.setTitle(ERROR_STRING);
                errorAlert.setHeaderText("Invalid input");
                errorAlert.setContentText(sb.toString());
                errorAlert.showAndWait();
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Confirm Update");
            alert.setContentText("Are you sure you want to update this expense details?\n"
            +"Expense Type from "+selectedExpense.getExpenseType()+" to "+expenseType+"\nAmount from "
                    +selectedExpense.getAmount()+"€ to "+amount+"€");
            if (alert.showAndWait().isPresent() && alert.getResult() == ButtonType.OK) {
                AuditLog auditLog = new AuditLog("Expense - " + selectedExpense.getId(),
                        "By: " + selectedExpense.getBy() + "|Expense Type: " + selectedExpense.getExpenseType()
                                + "|Amount: " + selectedExpense.getAmount() + "|Date: " + selectedExpense.getDate()
                                + "|Status: " + selectedExpense.getStatus() + "|Receipt File Path: " + selectedExpense.getReceiptFilePath(),
                        UserRole.ADMIN.name());
                selectedExpense.setExpenseType(expenseType);
                selectedExpense.setAmount(amount);
                expenseRepository.updateExpense(selectedExpense);
                auditLog.setNewValue("By: " + selectedExpense.getBy() + "|Expense Type: " + selectedExpense.getExpenseType()
                        + "|Amount: " + selectedExpense.getAmount() + "|Date: " + selectedExpense.getDate()
                        + "|Status: " + selectedExpense.getStatus() + "|Receipt File Path: " + selectedExpense.getReceiptFilePath());
                AuditLoggerUtil.saveLog(auditLog);
                loadExpenses();
            }
        }
    }
}
