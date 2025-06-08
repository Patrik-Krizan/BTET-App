package org.btet.database_repository;

import org.btet.enums.ExpenseStatus;
import org.btet.enums.ExpenseType;
import org.btet.model.Expense;
import org.btet.util.Database;
import org.btet.exception.RepositoryAccessException;
import org.btet.util.LoginUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * This class is responsible for handling all the database operations related to the Expense entity.
 * It provides methods to fetch, save, update and delete expenses from the database.
 * Doesn't inherit from AbstractRepository as it functions differently by providing multiple expenses in a single transaction.
 */
public class ExpenseRepository {
    private static Boolean databaseAccessInProgress = false;
    private static Logger logger = LoggerFactory.getLogger(ExpenseRepository.class);
    /**
     * This method fetches all the expenses from the database for the specified username.
     * @return List of expenses for the employee.
     * @param username The username of the employee whose expenses are to be fetched.
     * @throws RepositoryAccessException if there is an error accessing the database.
     */
    public synchronized List<Expense> findByUsername(String username) throws RepositoryAccessException {
        while (Boolean.TRUE.equals(databaseAccessInProgress)) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info("Error waiting for database access in findByUsername(): {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        databaseAccessInProgress = true;
        try (Connection connection = Database.connectToDatabase();
             PreparedStatement sqlStatement = connection.prepareStatement(
                     "SELECT ID,BY, EXPENSE_TYPE, AMOUNT, DATE, STATUS, RECEIPT_FILE_PATH FROM EXPENSE WHERE BY = ?")) {
            sqlStatement.setString(1, username);
            ResultSet resultSet = sqlStatement.executeQuery();
            List<Expense> expenses = new ArrayList<>();
            while (resultSet.next()) {
                expenses.add(getExpenseFromResultSet(resultSet));
            }
            return expenses;
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        } finally {
            databaseAccessInProgress = false;
            notifyAll();
        }
    }
    /**
     * This method fetches all the expenses from the database.
     * @return List of all expenses.
     * @throws RepositoryAccessException if there is an error accessing the database.
     */
    public synchronized List<Expense> findAll() throws RepositoryAccessException {
        while (Boolean.TRUE.equals(databaseAccessInProgress)) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info("Error waiting for database access in findAll(): {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        databaseAccessInProgress = true;
        try (Connection connection = Database.connectToDatabase();
             Statement sqlStatement = connection.createStatement();) {
            List<Expense> expenseList = new ArrayList<>();
            ResultSet resultSet = sqlStatement.executeQuery("SELECT ID,BY, EXPENSE_TYPE, AMOUNT, DATE, STATUS, RECEIPT_FILE_PATH FROM EXPENSE");
            while (resultSet.next()) {
                Expense expense = getExpenseFromResultSet(resultSet);
                expenseList.add(expense);
            }
            return expenseList;
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        } finally {
            databaseAccessInProgress = false;
            notifyAll();
        }
    }
    /**
     * This method fetches the expense from the result set.
     * @return Expense object
     * @param rs The result set from which the expense is to be fetched
     * @throws SQLException if there is an error accessing the database
     * */
    private static Expense getExpenseFromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("ID");
        String by = rs.getString("BY");
        ExpenseType expenseType = ExpenseType.valueOf(rs.getString("EXPENSE_TYPE"));
        BigDecimal amount = rs.getBigDecimal("AMOUNT");
        LocalDate date = rs.getDate("DATE").toLocalDate();
        ExpenseStatus status = ExpenseStatus.valueOf(rs.getString("STATUS"));
        String receiptFilePath = rs.getString("RECEIPT_FILE_PATH");
        return new Expense(id,by,expenseType, amount, date, status, receiptFilePath);
    }
    /**
     * Saves the list of expenses to the database.
     * @param expenses The list of expenses to be saved
     * @throws RepositoryAccessException if there is an error accessing the database
     * */
    public synchronized void save(List<Expense> expenses) throws RepositoryAccessException {
        while (Boolean.TRUE.equals(databaseAccessInProgress)) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info("Error waiting for database access in save(): {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        databaseAccessInProgress = true;
        try (Connection connection = Database.connectToDatabase();
             PreparedStatement sqlStatement = connection.prepareStatement(
                     "INSERT INTO EXPENSE (BY, EXPENSE_TYPE, AMOUNT, DATE, STATUS, RECEIPT_FILE_PATH) VALUES (?, ?, ?, ?, ?,?)")) {

            for (Expense expense : expenses) {
                sqlStatement.setString(1, LoginUtil.getLoggedUserRecord().username());
                sqlStatement.setString(2, expense.getExpenseType().name());
                sqlStatement.setBigDecimal(3, expense.getAmount());
                sqlStatement.setDate(4, Date.valueOf(LocalDate.now()));
                sqlStatement.setString(5, ExpenseStatus.PENDING.name());
                sqlStatement.setString(6, expense.getReceiptFilePath());
                sqlStatement.addBatch();
            }
            sqlStatement.executeBatch();
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        } finally {
            databaseAccessInProgress = false;
            notifyAll();
        }
    }
    /**
     * Saves the expense to the database, first adding it to a list and then calling the save method.
     * @param expense The expense to be saved.
     * */
    public void save(Expense expense) {
        List<Expense> expenses = new ArrayList<>();
        expenses.add(expense);
        save(expenses);
    }
    /**
     * Deletes the expense from the database.
     * @param id The id of the expense to be deleted.
     * @throws RepositoryAccessException if there is an error accessing the database.
     * */
    public synchronized void deleteById(Long id){
        while (Boolean.TRUE.equals(databaseAccessInProgress)) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info("Error waiting for database access in deleteById(): {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        databaseAccessInProgress = true;
        try (Connection connection = Database.connectToDatabase();
             PreparedStatement sqlStatement = connection.prepareStatement(
                     "DELETE FROM EXPENSE WHERE ID = ?")) {
            sqlStatement.setLong(1, id);
            sqlStatement.executeUpdate();
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        } finally {
            databaseAccessInProgress = false;
            notifyAll();
        }
    }
    /**
     * Updates the status of the expense in the database(APPROVED/REJECTED).
     * @param id The id of the expense whose status is to be updated.
     * @param status The new status of the expense.
     * @throws RepositoryAccessException if there is an error accessing the database.
     * */
    public synchronized void updateStatus(Long id, ExpenseStatus status){
        while (Boolean.TRUE.equals(databaseAccessInProgress)) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info("Error waiting for database access in updateStatus(): {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        databaseAccessInProgress = true;
        try (Connection connection = Database.connectToDatabase();
             PreparedStatement sqlStatement = connection.prepareStatement(
                     "UPDATE EXPENSE SET STATUS = ? WHERE ID = ?")) {
            sqlStatement.setString(1, status.name());
            sqlStatement.setLong(2, id);
            sqlStatement.executeUpdate();
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        } finally {
            databaseAccessInProgress = false;
            notifyAll();
        }
    }
    /**
     * Updates the expense in the database.
     * @param expense The expense to be updated.
     * @throws RepositoryAccessException if there is an error accessing the database.
     * */
    public synchronized void updateExpense(Expense expense){
        while (Boolean.TRUE.equals(databaseAccessInProgress)) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info("Error waiting for database access in updateStatus(): {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        databaseAccessInProgress = true;
        try (Connection connection = Database.connectToDatabase();
             PreparedStatement sqlStatement = connection.prepareStatement(
                     "UPDATE EXPENSE SET EXPENSE_TYPE = ?, AMOUNT = ? WHERE ID = ?")) {
            sqlStatement.setString(1, expense.getExpenseType().name());
            sqlStatement.setBigDecimal(2, expense.getAmount());
            sqlStatement.setLong(3, expense.getId());
            sqlStatement.executeUpdate();
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        } finally {
            databaseAccessInProgress = false;
            notifyAll();
        }
    }
}
