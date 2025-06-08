package org.btet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.btet.database_repository.EmployeeRepository;
import org.btet.database_repository.ExpenseRepository;
import org.btet.model.Employee;
import org.btet.model.EmployeeExpenseStats;
import org.btet.model.Expense;
import org.btet.util.LoginUtil;

import java.util.List;
/**
 * Controller class for the Employee Stats Popup, which is a popup that displays the statistics of the logged in employee.
 * This popup displays time based expense statistics, total expenses of the employee, number of expenses,
 * and the official report of the expenses.
 */
public class EmployeeStatsPopupController {
    @FXML
    private Label todayLabel;
    @FXML
    private Label monthlyLabel;
    @FXML
    private Label yearlyLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label numberLabel;
    @FXML
    private Label reportLabel;
    private static final EmployeeRepository<Employee> employeeRepository = new EmployeeRepository<>();
    private static final ExpenseRepository expenseRepository = new ExpenseRepository();
    /**
     * Initializes the Employee Stats Popup, gets the logged in employee, gets the expenses of the employee,
     * creates an EmployeeExpenseStats object, sets the values of the labels in the popup, and generates the report.
     * This method is called automatically when the FXML file is loaded.
     */
    public void initialize(){
        Employee employee = employeeRepository.findByUsername(LoginUtil.getLoggedUserRecord().username());
        List<Expense> expenses = expenseRepository.findByUsername(employee.getUsername());
        EmployeeExpenseStats<Employee, List<Expense>> employeeExpenseStats = new EmployeeExpenseStats<>(employee, expenses);
        todayLabel.setText(employeeExpenseStats.getTodaysExpenseSum().toString());
        monthlyLabel.setText(employeeExpenseStats.getMonthlyExpenseSum().toString());
        yearlyLabel.setText(employeeExpenseStats.getYearlyExpenseSum().toString());
        totalLabel.setText(employeeExpenseStats.getTotalExpenseSum().toString());
        numberLabel.setText(String.valueOf(employeeExpenseStats.getNumberOfExpenses()));
        reportLabel.setText(employeeExpenseStats.generateReport());
        reportLabel.setWrapText(true);
        reportLabel.setStyle("-fx-text-alignment: center;");
    }
}
