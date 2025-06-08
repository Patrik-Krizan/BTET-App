package org.btet.model;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
/**
 * A class that represents a reportable employee expense statistics, using generic parameters for employee and expense types,
 * giving the ability to create a report for a specific employee and their expenses(flexibility).
 * It contains methods for calculating monthly, yearly, today's and total expense sum, as well as the number of expenses.
 * It implements the Reportable interface, which contains a method for generating a report.
 * */
public class EmployeeExpenseStats <T extends Employee, S extends List<Expense>> implements Reportable{
    private T employee;
    private S expenses;
    private static final String APPROVED = "APPROVED";
    /**
     * Constructor for the EmployeeExpenseStats class.
     * @param employee the employee for which the reportable statistics are created.
     * @param expenses the list of expenses for the employee.
     * */
    public EmployeeExpenseStats(T employee, S expenses) {
        this.employee = employee;
        this.expenses = expenses;
    }

    public T getEmployee() {
        return employee;
    }

    public S getExpenses() {
        return expenses;
    }

    public BigDecimal getMonthlyExpenseSum() {
        return expenses.stream()
                .filter(expense -> expense.getDate().getMonth().equals(LocalDate.now().getMonth()))
                .filter(expense -> expense.getStatus().name().equals(APPROVED))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getYearlyExpenseSum() {
        return expenses.stream()
                .filter(expense -> expense.getDate().getYear() == LocalDate.now().getYear())
                .filter(expense -> expense.getStatus().name().equals(APPROVED))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTodaysExpenseSum() {
        return expenses.stream()
                .filter(expense -> expense.getDate().equals(LocalDate.now()))
                .filter(expense -> expense.getStatus().name().equals(APPROVED))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpenseSum() {
        return expenses.stream()
                .filter(expense -> expense.getStatus().name().equals(APPROVED))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getNumberOfExpenses() {
        return expenses.size();
    }


    @Override
    public String generateReport() {
        return "This is an automated report for a reportable expenses for employee: " +employee.getUsername()+ ". All rights reserved by BTET. " +
                "Any unauthorized use is strictly prohibited! " + "Generated on " + LocalDate.now().
                format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))+".";
    }
}
