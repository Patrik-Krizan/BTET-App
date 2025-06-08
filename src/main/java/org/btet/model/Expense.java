package org.btet.model;

import org.btet.enums.ExpenseStatus;
import org.btet.enums.ExpenseType;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * The Expense class is a model class that represents an expense which each employee can add to the system.
 * After the addition, the manager reviews the expense and approves or rejects it.
 * Each expense has an id, the name of the employee who added the expense, the type of the expense, the amount of the expense,
 * the date of the expense, the status of the expense, and the file path of the receipt of the expense.
 * The Expense class implements the Taxable interface to add tax to the amount of the expense if the expense type is OTHER.
 */
public non-sealed class Expense implements Taxable{
    private Long id;
    private String by;
    private ExpenseType expenseType;
    private BigDecimal amount;
    private LocalDate date;
    private ExpenseStatus status;
    private String receiptFilePath;
    /**
     * The constructor of the Expense class.
     * @param id The id of the expense.
     * @param by The name of the employee who added the expense.
     * @param expenseType The type of the expense.
     * @param amount The amount of the expense.
     * @param date The date of the expense.
     * @param status The status of the expense.
     * @param receiptFilePath The file path of the receipt of the expense.
     */
    public Expense(Long id, String by, ExpenseType expenseType, BigDecimal amount,
                   LocalDate date, ExpenseStatus status, String receiptFilePath) {
        this.expenseType = expenseType;
        this.by = by;
        this.amount = amount;
        this.date = date;
        this.status = status;
        this.id = id;
        this.receiptFilePath = receiptFilePath;
    }

    @Override
    public void addTax(BigDecimal taxPercentage) {
        if(expenseType == ExpenseType.OTHER)  this.amount = amount.multiply(taxPercentage.add(BigDecimal.ONE));
    }

    public ExpenseType getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(ExpenseType expenseType) {
        this.expenseType = expenseType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDateTime(LocalDate date) {
        this.date = date;
    }

    public ExpenseStatus getStatus() {
        return status;
    }

    public void setStatus(ExpenseStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String getReceiptFilePath() {
        return receiptFilePath;
    }

    public void setReceiptFilePath(String receiptFilePath) {
        this.receiptFilePath = receiptFilePath;
    }
}
