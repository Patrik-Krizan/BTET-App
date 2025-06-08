package org.btet.database_repository;
import org.btet.model.Budget;
import org.btet.util.Database;
import org.btet.exception.RepositoryAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * BudgetRepository class is a repository class that extends AbstractRepository class and is responsible for
 * handling all the database operations related to Budget objects.
 * It provides methods to find a budget by username, find all budgets, save a budget, save a list of budgets,
 * update the spent amount of a budget, update the allocated amount of a budget and delete a budget by username.
 * It also provides a method to get a Budget object from a ResultSet object.
 * @param <T> a Budget object
 */
public class BudgetRepository<T extends Budget> extends AbstractRepository<T> {
    private static Boolean databaseAccessInProgress = false;
    private static Logger logger = LoggerFactory.getLogger(BudgetRepository.class.getName());
    @Override
    public synchronized T findByUsername(String username) throws RepositoryAccessException {
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
                "SELECT ALLOCATED_AMOUNT, SPENT_AMOUNT, GIVEN_BY, GIVEN_TO FROM BUDGET WHERE GIVEN_TO = ?")) {
            sqlStatement.setString(1, username);
            ResultSet resultSet = sqlStatement.executeQuery();
            if (resultSet.next()) {
                return (T)getBudgetFromResultSet(resultSet);
            } else {
                logger.info("Budget for user {} not found", username);
                return null;
            }
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        } finally {
            databaseAccessInProgress = false;
            notifyAll();
        }
    }
    @Override
    public synchronized List<T> findAll() throws RepositoryAccessException {
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
             Statement sqlStatement = connection.createStatement()) {
            List<T> budgetList = new ArrayList<>();
            ResultSet resultSet = sqlStatement.executeQuery("SELECT ALLOCATED_AMOUNT, SPENT_AMOUNT, GIVEN_BY, GIVEN_TO FROM BUDGET");
            while (resultSet.next()) {
                T budget = (T)getBudgetFromResultSet(resultSet);
                budgetList.add(budget);
            }
            return budgetList;
        } catch (SQLException | IOException e) {
            throw new RepositoryAccessException(e);
        } finally {
            databaseAccessInProgress = false;
            notifyAll();
        }
    }
    /**
     * Returns a Budget object from a ResultSet object.
     * @param rs a ResultSet object
     * @return a Budget object
     * @throws SQLException if an error occurs while accessing the ResultSet object
     */
    private static Budget getBudgetFromResultSet(ResultSet rs) throws SQLException {
        BigDecimal allocatedAmount = rs.getBigDecimal("ALLOCATED_AMOUNT");
        BigDecimal spentAmount = rs.getBigDecimal("SPENT_AMOUNT");
        String givenBy = rs.getString("GIVEN_BY");
        String givenTo = rs.getString("GIVEN_TO");
        Budget budget = new Budget.Builder().allocateBudgetAmount(allocatedAmount).givenTo(givenTo)
                .givenBy(givenBy).build();
        budget.setSpentAmount(spentAmount);
        return budget;
    }
    @Override
    public synchronized void save(List<T> budgets) {
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
                     "INSERT INTO BUDGET (ALLOCATED_AMOUNT, SPENT_AMOUNT, GIVEN_BY, GIVEN_TO) VALUES (?, ?, ?, ?)")) {
            for (T budget : budgets) {
                sqlStatement.setBigDecimal(1, budget.getAllocatedAmount());
                sqlStatement.setBigDecimal(2, budget.getSpentAmount());
                sqlStatement.setString(3, budget.getGivenBy());
                sqlStatement.setString(4, budget.getGivenTo());
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
    @Override
    public void save(T budget) {
        List<T> budgets = new ArrayList<>();
        budgets.add(budget);
        save(budgets);
    }
    /**
     *Updates the spent amount of a budget in the database.
     * @param username a username whose budget's spent amount needs to be updated
     * @throws RepositoryAccessException if an error occurs while accessing the database
     */
    public synchronized void updateSpentAmount(String username) throws RepositoryAccessException {
        while (Boolean.TRUE.equals(databaseAccessInProgress)) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info("Error waiting for database access in updateSpentAmount(): {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        databaseAccessInProgress = true;
        try (Connection connection = Database.connectToDatabase();
             PreparedStatement sqlStatement = connection.prepareStatement("UPDATE BUDGET SET SPENT_AMOUNT = " +
                     "COALESCE((SELECT SUM(AMOUNT) FROM EXPENSE WHERE BY = ? AND STATUS = 'APPROVED'), 0) WHERE GIVEN_TO = ?");) {
            sqlStatement.setString(1, username);
            sqlStatement.setString(2, username);
            sqlStatement.executeUpdate();
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        } finally {
            databaseAccessInProgress = false;
            notifyAll();
        }
    }
    /**
     * Updates the allocated amount of a budget in the database.
     * @param username a username whose budget's allocated amount needs to be updated
     * @param allocatedAmount an allocated amount to be updated
     * @throws RepositoryAccessException if an error occurs while accessing the database
     */
    public synchronized void updateAllocatedAmount(String username, BigDecimal allocatedAmount) throws RepositoryAccessException {
        while (Boolean.TRUE.equals(databaseAccessInProgress)) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info("Error waiting for database access in updateAllocatedAmount(): {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        databaseAccessInProgress = true;
        try (Connection connection = Database.connectToDatabase();
             PreparedStatement sqlStatement = connection.prepareStatement(
                     "UPDATE BUDGET SET ALLOCATED_AMOUNT = ? WHERE GIVEN_TO = ?")) {
            sqlStatement.setBigDecimal(1, allocatedAmount);
            sqlStatement.setString(2, username);
            sqlStatement.executeUpdate();
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        } finally {
            databaseAccessInProgress = false;
            notifyAll();
        }
    }
    /**
     * Deletes a budget by username from the database.
     * @param username a username whose budget needs to be deleted
     * @throws RepositoryAccessException if an error occurs while accessing the database
     */
    public synchronized void deleteByUsername(String username) throws RepositoryAccessException {
        while (Boolean.TRUE.equals(databaseAccessInProgress)) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info("Error waiting for database access in deleteByUsername(): {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        databaseAccessInProgress = true;
        try (Connection connection = Database.connectToDatabase();
             PreparedStatement sqlStatement = connection.prepareStatement(
                     "DELETE FROM BUDGET WHERE GIVEN_TO = ?")) {
            sqlStatement.setString(1, username);
            sqlStatement.executeUpdate();
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        } finally {
            databaseAccessInProgress = false;
            notifyAll();
        }
    }
}