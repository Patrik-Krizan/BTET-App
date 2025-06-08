package org.btet.database_repository;
import org.btet.model.Manager;
import org.btet.util.Database;
import org.btet.exception.RepositoryAccessException;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * ManagerRepository class is a repository class that extends AbstractRepository class, and is responsible for
 * handling all the database operations related to Manager objects.
 * It provides methods to find a manager by username, find all managers, save a manager, save a list of managers,
 * update a manager, and delete a manager by username.
 * It also provides a method to get a Manager object from a ResultSet object.
 * @param <T> a Manager object
 */
public class ManagerRepository<T extends Manager> extends AbstractRepository<T> {
    /**
     * This method gets a Manager object from a ResultSet object.
     * @param rs a ResultSet object
     * @return a Manager object
     * @throws SQLException if a database access error occurs
     */
    private static Manager getManagerFromResultSet(ResultSet rs) throws SQLException {
        String name = rs.getString("NAME");
        String username = rs.getString("USERNAME");
        String department = rs.getString("DEPARTMENT");
        return new Manager(name, username, department);
    }

    @Override
    public synchronized T findByUsername(String username) throws RepositoryAccessException {
        try (Connection connection = Database.connectToDatabase();
             PreparedStatement sqlStatement = connection.prepareStatement(
                     "SELECT NAME, USERNAME, DEPARTMENT FROM MANAGER WHERE USERNAME = ?")) {
            sqlStatement.setString(1, username);
            ResultSet resultSet = sqlStatement.executeQuery();
            if (resultSet.next()) {
                return (T)getManagerFromResultSet(resultSet);
            } else {
                throw new RepositoryAccessException("Manager with username " + username + " not found");
            }
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        }
    }

    @Override
    public synchronized List<T> findAll() throws RepositoryAccessException {
        try (Connection connection = Database.connectToDatabase();
             Statement sqlStatement = connection.createStatement()) {
            List<T> managerList = new ArrayList<>();
            ResultSet resultSet = sqlStatement.executeQuery("SELECT NAME, USERNAME, DEPARTMENT FROM MANAGER");
            while (resultSet.next()) {
                Manager manager = getManagerFromResultSet(resultSet);
                managerList.add((T)manager);
            }
            return managerList;
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        }
    }

    @Override
    public void save(T manager) {
        List<T> managers = new ArrayList<>();
        managers.add(manager);
        save(managers);
    }

    @Override
    public synchronized void save(List<T> managers) {
        try (Connection connection = Database.connectToDatabase();
             PreparedStatement sqlStatement = connection.prepareStatement(
                     "INSERT INTO MANAGER (NAME, USERNAME, DEPARTMENT) VALUES (?, ?, ?)")) {
            for (Manager manager : managers) {
                sqlStatement.setString(1, manager.getName());
                sqlStatement.setString(2, manager.getUsername());
                sqlStatement.setString(3, manager.getDepartment());
                sqlStatement.addBatch();
            }
            sqlStatement.executeBatch();
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        }
    }
    /**
     * This method updates a Manager object in the database.
     * @param manager a new Manager object to replace the old Manager object
     * @param oldUsername the old username of the Manager object so that it can be identified in the database
     * @throws RepositoryAccessException if a database access error occurs
     */
    public void update(Manager manager, String oldUsername) {
        try (Connection connection = Database.connectToDatabase()) {
            Statement disableIntegrityStatement = connection.createStatement();
            disableIntegrityStatement.execute("SET REFERENTIAL_INTEGRITY FALSE");

            PreparedStatement updateStatementManagerTable = connection.prepareStatement(
                    "UPDATE MANAGER SET NAME = ?, USERNAME = ?, DEPARTMENT = ? WHERE USERNAME = ?");
            updateStatementManagerTable.setString(1, manager.getName());
            updateStatementManagerTable.setString(2, manager.getUsername());
            updateStatementManagerTable.setString(3, manager.getDepartment());
            updateStatementManagerTable.setString(4, oldUsername);
            updateStatementManagerTable.executeUpdate();

            PreparedStatement updateStatementBudgetTable = connection.prepareStatement(
                    "UPDATE BUDGET SET GIVEN_TO = ? WHERE GIVEN_TO = ?");
            updateStatementBudgetTable.setString(1, manager.getUsername());
            updateStatementBudgetTable.setString(2, oldUsername);
            updateStatementBudgetTable.executeUpdate();

            Statement enableIntegrityStatement = connection.createStatement();
            enableIntegrityStatement.execute("SET REFERENTIAL_INTEGRITY TRUE");
        } catch (SQLException | IOException e) {
            throw new RepositoryAccessException(e);
        }
    }
    /**
     * Deletes a Manager object from the database.
     * @param username the username of the Manager object to be deleted
     * @throws RepositoryAccessException if a database access error occurs
     */
    public void delete(String username) {
        try (Connection connection = Database.connectToDatabase()) {
            PreparedStatement deleteStatementBudgetTable = connection.prepareStatement(
                    "DELETE FROM BUDGET WHERE GIVEN_BY = ?");
            deleteStatementBudgetTable.setString(1, username);
            deleteStatementBudgetTable.executeUpdate();

            PreparedStatement deleteStatementManagerTable = connection.prepareStatement(
                    "DELETE FROM MANAGER WHERE USERNAME = ?");
            deleteStatementManagerTable.setString(1, username);
            deleteStatementManagerTable.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RepositoryAccessException(e);
        }
    }
}