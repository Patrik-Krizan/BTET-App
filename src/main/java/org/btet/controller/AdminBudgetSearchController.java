package org.btet.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.btet.database_repository.BudgetRepository;
import org.btet.model.Budget;
import org.btet.util.ValidationUtil;
import org.btet.exception.RepositoryAccessException;
import org.btet.model.AuditLog;
import org.btet.util.AuditLoggerUtil;
import org.btet.enums.UserRole;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;
/**
 * Controller class for AdminBudgetSearch.fxml, handles the search and update of budgets.
 * */
public class AdminBudgetSearchController {
    @FXML
    private TextField amountUpdateField;
    @FXML
    private TextField allocatedFromFilterField;
    @FXML
    private TextField allocatedToFilterField;
    @FXML
    private TextField spentFromFilterField;
    @FXML
    private TextField spentToFilterField;
    @FXML
    private TextField givenToFilterField;
    @FXML
    private TextField givenByFilterField;
    @FXML
    private TableView<Budget> budgetTable;
    @FXML
    private TableColumn<Budget, String> allocatedAmountColumn;
    @FXML
    private TableColumn<Budget, String> spentAmountColumn;
    @FXML
    private TableColumn<Budget, String> givenByColumn;
    @FXML
    private TableColumn<Budget, String> givenToColumn;

    private static final String ERROR_STRING = "Error";
    private static Alert errorAlert = new Alert(Alert.AlertType.ERROR);
    private BudgetRepository<Budget> budgetRepository = new BudgetRepository<>();
    private static Logger logger = Logger.getLogger(AdminBudgetSearchController.class.getName());
    /**
     * initializes the table and loads the budgets, sets the cell value factories for the columns.
     * Sets the on click listener for the table to update the amount field.
     * */
    public void initialize() {
        allocatedAmountColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAllocatedAmount().toString()));
        spentAmountColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSpentAmount().toString()));
        givenByColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getGivenBy()));
        givenToColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getGivenTo()));
        budgetTable.getColumns().forEach(column -> column.setStyle("-fx-alignment: CENTER;"));
        budgetTable.setOnMouseClicked(event -> {
            Budget selectedBudget = budgetTable.getSelectionModel().getSelectedItem();
            if (selectedBudget != null) {
                amountUpdateField.setText(selectedBudget.getAllocatedAmount().toString());
            }
        });
        loadBudgets();
    }
    /**
     * Loads all budgets from the database and sets the table items.
     * */
    private void loadBudgets() {
        try {
            List<Budget> budgets = budgetRepository.findAll();
            ObservableList<Budget> budgetObservableList = FXCollections.observableList(budgets);
            budgetTable.setItems(budgetObservableList);
        } catch (RepositoryAccessException e) {
            logger.info("Error loading budgets: " + e.getMessage());
            showAlert("Error loading budgets: " + e.getMessage());
        }
    }
    /**
     * Filters the budgets based on the filter fields.
     * */
    public void filterBudgets() {
        try {
            List<Budget> allBudgets = budgetRepository.findAll();
            String allocatedFromText = allocatedFromFilterField.getText().trim();
            String allocatedToText = allocatedToFilterField.getText().trim();
            String spentFromText = spentFromFilterField.getText().trim();
            String spentToText = spentToFilterField.getText().trim();
            String givenByText = givenByFilterField.getText().trim();
            String givenToText = givenToFilterField.getText().trim();

            BigDecimal allocatedFrom = ValidationUtil.parseBigDecimal(allocatedFromText);
            BigDecimal allocatedTo = ValidationUtil.parseBigDecimal(allocatedToText);
            BigDecimal spentFrom = ValidationUtil.parseBigDecimal(spentFromText);
            BigDecimal spentTo = ValidationUtil.parseBigDecimal(spentToText);

            List<Budget> filteredBudgets = allBudgets.stream()
                    .filter(budget -> givenByText.isEmpty() || budget.getGivenBy().contains(givenByText))
                    .filter(budget -> givenToText.isEmpty() || budget.getGivenTo().contains(givenToText))
                    .filter(budget -> allocatedFrom == null || budget.getAllocatedAmount().compareTo(allocatedFrom) >= 0)
                    .filter(budget -> allocatedTo == null || budget.getAllocatedAmount().compareTo(allocatedTo) <= 0)
                    .filter(budget -> spentFrom == null || budget.getSpentAmount().compareTo(spentFrom) >= 0)
                    .filter(budget -> spentTo == null || budget.getSpentAmount().compareTo(spentTo) <= 0)
                    .toList();

            budgetTable.setItems(FXCollections.observableList(filteredBudgets));
        } catch (RepositoryAccessException e) {
            logger.info("Error filtering budgets: " + e.getMessage());
            showAlert("Error filtering budgets: " + e.getMessage());
        }
    }
    /**
     * Updates the allocated amount of the selected budget.
     * Shows an alert if the amount is invalid.
     * Adds an audit log for the update.
     * */
    public void updateBudget() {
        Budget selectedBudget = budgetTable.getSelectionModel().getSelectedItem();
        try {
            if (selectedBudget != null) {
                StringBuilder sb = new StringBuilder();

                String amountString = amountUpdateField.getText();
                BigDecimal allocatedAmount = ValidationUtil.parseBigDecimal(amountString);
                if (allocatedAmount == null || allocatedAmount.compareTo(BigDecimal.ZERO) < 0 ||
                        allocatedAmount.compareTo(BigDecimal.valueOf(1000000)) >= 1) {
                    sb.append("Invalid amount entered. Must be in bounds[0, 1 000 000]\n");
                }

                if (!sb.isEmpty()) {
                    showAlert(sb.toString());
                    return;
                }

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Alert");
                alert.setHeaderText("Confirm Update");
                alert.setContentText("Are you sure you want to update the allocated amount?\n" +
                        "Old value: " + selectedBudget.getAllocatedAmount() + "\n" +
                        "New value: " + allocatedAmount + "\nThis action will be logged and must be confirmed with a manager.");

                if (alert.showAndWait().isPresent() && alert.getResult() == ButtonType.OK) {
                    AuditLog auditLog = new AuditLog("Budget - " + selectedBudget.getGivenTo(),
                            selectedBudget.getAllocatedAmount().toString(), UserRole.ADMIN.name());
                    budgetRepository.updateAllocatedAmount(selectedBudget.getGivenTo(), allocatedAmount);
                    selectedBudget.setAllocatedAmount(allocatedAmount);
                    auditLog.setNewValue(selectedBudget.getAllocatedAmount().toString());
                    AuditLoggerUtil.saveLog(auditLog);
                    loadBudgets();
                }
            }
        }catch (RepositoryAccessException e) {
            logger.info("Error updating budget: " + e.getMessage());
            showAlert("Error updating budget: " + e.getMessage());
        }
    }
    /**
     * Shows an alert with the given message.
     * @param message the message to be shown in the alert.
     * */
    private void showAlert(String message) {
        errorAlert.setTitle(ERROR_STRING);
        errorAlert.setHeaderText("Error occurred");
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
}