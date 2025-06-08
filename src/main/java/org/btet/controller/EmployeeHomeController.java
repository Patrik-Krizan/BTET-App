package org.btet.controller;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import org.btet.database_repository.ExpenseRepository;
import org.btet.enums.ExpenseStatus;
import org.btet.enums.ExpenseType;
import org.btet.exception.RepositoryAccessException;
import org.btet.model.Expense;
import org.btet.model.UserLoginRecord;
import org.btet.util.LoginUtil;
import org.btet.util.ReceiptFileUtil;
import org.btet.util.ScreenManager;
import org.btet.util.ValidationUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
/**
 * Controller class for the employee home screen, which is the first screen that the employee sees after logging in.
 * This screen allows the employee to add expenses, view their expenses, filter their expenses, delete pending expenses,
 * and view their budget if they have one;
 */
public class EmployeeHomeController {
    @FXML private Label welcomeLabel;
    @FXML private ComboBox<String> addExpenseTypeComboBox;
    @FXML private TextField addAmountField;
    @FXML private TableView<Expense> expenseTableView;
    @FXML private TableColumn<Expense, String> idTableColumn;
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
    @FXML private MenuItem budgetMenuItem;
    @FXML private Button uploadReceiptButton;
    @FXML private TextField receiptPathField;
    @FXML private Label highestExpenseLabel;
    @FXML private Label lowestExpenseLabel;

    private  UserLoginRecord userLoginRecord = LoginUtil.getLoggedUserRecord();
    private static final ExpenseRepository expenseRepository = new ExpenseRepository();
    private static final String NO_CHOICE_STRING = "<no choice>";
    private String receiptPath;
    private static Logger logger = Logger.getLogger(EmployeeHomeController.class.getName());
    /**
     * Initializes the employee home screen, sets cell values for the expense table, loads the expenses,
     * sets up a Timeline thread to refresh the expenses every 15 seconds, and sets up a Timeline thread to
     * display the highest and lowest expenses every 25 seconds --> displaying synchronization.
     * This method is called automatically when the FXML file is loaded.
     */
    public void initialize() {
        budgetMenuItem.setOnAction(value -> ScreenManager.popupScreen("/fxml/EmployeeHomeBudgetPopup.fxml"));
        welcomeLabel.setText(userLoginRecord.username() + "|" + userLoginRecord.role());
        addExpenseTypeComboBox.getItems().addAll(Stream.of(ExpenseType.values()).map(Enum::name).toList());
        filterExpenseTypeComboBox.getItems().add(NO_CHOICE_STRING);
        filterExpenseTypeComboBox.getItems().addAll(Stream.of(ExpenseType.values()).map(Enum::name).toList());
        filterExpenseTypeComboBox.getSelectionModel().select(0);
        statusFilterComboBox.getItems().add(NO_CHOICE_STRING);
        statusFilterComboBox.getItems().addAll(Stream.of(ExpenseStatus.values()).map(Enum::name).toList());
        statusFilterComboBox.getSelectionModel().select(0);
        idTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId().toString()));
        expenseTypeTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getExpenseType().name()));
        amountTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmount().toString()));
        dateTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate().toString()));
        statusTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStatus().name()));
        expenseTableView.getColumns().forEach(c -> c.setStyle("-fx-alignment: CENTER;"));
        Timeline refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(15), event -> loadExpenses()));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();

        Timeline highestExpenseTimeline = new Timeline(new KeyFrame(Duration.seconds(15), event -> {
            Expense highestExpense = expenseRepository.findByUsername(userLoginRecord.username()).stream()
                    .max(Comparator.comparing(Expense::getAmount)).orElse(null);
            if (highestExpense != null) {
                highestExpenseLabel.setText("Highest expense: " + highestExpense.getAmount() + "€");
            } else {
                highestExpenseLabel.setText("No expenses");
            }
        }));
        highestExpenseTimeline.setCycleCount(Animation.INDEFINITE);
        highestExpenseTimeline.play();

        Timeline lowestExpenseTimeline = new Timeline(new KeyFrame(Duration.seconds(15), event -> {
            Expense lowestExpense = expenseRepository.findByUsername(userLoginRecord.username()).stream()
                    .min(Comparator.comparing(Expense::getAmount)).orElse(null);
            if (lowestExpense != null) {
                lowestExpenseLabel.setText("Lowest expense: " + lowestExpense.getAmount() + "€");
            } else {
                lowestExpenseLabel.setText("No expenses");
            }
        }));
        lowestExpenseTimeline.setCycleCount(Animation.INDEFINITE);
        lowestExpenseTimeline.play();

        loadExpenses();
    }
    /**
     * Loads the expenses from the database and displays them in the expense table.
     */
    private void loadExpenses() {
        try{
        List<Expense> expenseList = expenseRepository.findByUsername(userLoginRecord.username());
        ObservableList<Expense> expenseObservableList = FXCollections.observableList(expenseList);
        expenseTableView.setItems(expenseObservableList);
        }catch (RepositoryAccessException e){
            logger.warning("Error loading expenses: " + e.getMessage());
        }
    }
    /**
     * Uploads the receipt file and sets the receipt path in the receiptPathField, using the ReceiptFileUtil class.
     */
    public void uploadReceipt(){
        receiptPath = ReceiptFileUtil.returnReceiptPath(uploadReceiptButton);
        receiptPathField.setText(receiptPath);
    }
    /**
     * Filters the expenses based on the filter fields and displays the filtered expenses in the expense table.
     */
    public void filterExpenses() {
        List<Expense> allExpenses = expenseRepository.findByUsername(userLoginRecord.username());
        String idFilterText = filterIdField.getText().trim();
        String expenseTypeFilter = filterExpenseTypeComboBox.getValue();
        String fromAmountText = fromAmountFilterField.getText().trim();
        String toAmountText = toAmountFilterField.getText().trim();
        LocalDate fromDate = fromDateFilterField.getValue();
        LocalDate toDate = toDateFilterField.getValue();
        String statusFilter = statusFilterComboBox.getValue();
        BigDecimal fromAmount = ValidationUtil.parseBigDecimal(fromAmountText);
        BigDecimal toAmount = ValidationUtil.parseBigDecimal(toAmountText);
        List<Expense> filteredExpenses = allExpenses.stream()
                .filter(expense -> idFilterText.isEmpty() || String.valueOf(expense.getId()).contains(idFilterText))
                .filter(expense -> expenseTypeFilter.equals(NO_CHOICE_STRING) || expense.getExpenseType().name().equals(expenseTypeFilter))
                .filter(expense -> statusFilter.equals(NO_CHOICE_STRING) || expense.getStatus().name().equals(statusFilter))
                .filter(expense -> fromAmount == null || expense.getAmount().compareTo(fromAmount) >= 0)
                .filter(expense -> toAmount == null || expense.getAmount().compareTo(toAmount) <= 0)
                .filter(expense -> fromDate == null || !expense.getDate().isBefore(fromDate))
                .filter(expense -> toDate == null || !expense.getDate().isAfter(toDate)).toList();
        expenseTableView.setItems(FXCollections.observableList(filteredExpenses));
    }
    /**
     * Adds an expense to the system based on the values inputted in the add expense fields.
     * If the values are invalid, an error message is displayed.
     */
    public void addExpense() {
        String expenseTypeString = addExpenseTypeComboBox.getValue();
        StringBuilder sb = new StringBuilder();
        ExpenseType expenseType = null;
        if (expenseTypeString == null || expenseTypeString.isEmpty()) {
            sb.append("Expense type cannot be empty.\n");
        }else expenseType = ExpenseType.valueOf(expenseTypeString);
        String amountString = addAmountField.getText();
        if (amountString.isEmpty()) {
            sb.append("Amount cannot be empty.\n");
        }
        BigDecimal amount = ValidationUtil.parseBigDecimal(amountString);
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(BigDecimal.valueOf(100000)) > 0){
            sb.append("Invalid amount format. Make sure the amount is in bounds[0, 100 000]\n");
        }

        if(receiptPath == null || receiptPath.isEmpty()){
            sb.append("Receipt must be uploaded.\n");
        }

        if(!sb.isEmpty()) {
            showAlert(sb.toString());
        }
        else{
            Expense newExpense = new Expense(null,userLoginRecord.username(),expenseType, amount,
                    LocalDate.now(), ExpenseStatus.PENDING, receiptPath);
            newExpense.addTax(BigDecimal.valueOf(0.25));
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Add expense");
            alert.setHeaderText("Are you sure you want to add this expense?");
            alert.setContentText("Expense type: " + expenseType + "\nAmount: " + amount+"€");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    expenseRepository.save(newExpense);
                    loadExpenses();
                    clearAddExpenseFields();
                }
            });
        }
    }
    /**
     * Displays an error message in an alert dialog.
     * @param message the message to display in the alert dialog
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
    /**
     * Clears the expense fields used for adding a new expense, once the expense has been added.
     */
    private void clearAddExpenseFields() {
        addExpenseTypeComboBox.getSelectionModel().clearSelection();
        addAmountField.clear();
        receiptPathField.clear();
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
    /**
     * Deletes the selected pending expense from the system, if one is selected.
     * If no expense is selected, an error message is displayed.
     */
    public void deletePending(){
        Expense selectedExpense = expenseTableView.getSelectionModel().getSelectedItem();
        if (selectedExpense != null){
            if(selectedExpense.getStatus().equals(ExpenseStatus.PENDING)){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete expense");
                alert.setHeaderText("Are you sure you want to delete this expense?");
                alert.setContentText("Expense type: " + selectedExpense.getExpenseType() + "\nAmount: " + selectedExpense.getAmount()+"€");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        expenseRepository.deleteById(selectedExpense.getId());
                        loadExpenses();
                    }
                });
            } else{
                showAlert("Only pending expenses can be deleted.");
            }
        } else{
            showAlert("No expense selected.");
        }
    }
    /**
     * Opens the employee stats popup screen, which displays statistics about the employee's expenses.
     */
    public void openStats(){
        ScreenManager.popupScreen("/fxml/EmployeeStatsPopup.fxml");
    }
}