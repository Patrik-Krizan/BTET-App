package org.btet.database_repository;
import org.btet.model.Employee;
import org.btet.util.Database;
import org.btet.exception.RepositoryAccessException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a repository for Employee objects. It extends the AbstractRepository class and implements the
 * findByUsername and findAll methods. It also implements the save method to save a list of Employee objects to the
 * database. The class also has a method to update an Employee object in the database and a method to delete an Employee
 * object from the database.
 */
public class EmployeeRepository<T extends Employee> extends AbstractRepository<T> {
    @Override
    public  T findByUsername(String username) throws RepositoryAccessException {
        try (Connection connection = Database.connectToDatabase();
             PreparedStatement sqlStatement = connection.prepareStatement(
                     "SELECT NAME,USERNAME,EMAIL,PHONE FROM EMPLOYEE WHERE USERNAME = ?")) {
            sqlStatement.setString(1, username);
            ResultSet resultSet = sqlStatement.executeQuery();
            if (resultSet.next()) {
                return (T)getEmployeeFromResultSet(resultSet);
            } else {
                throw new RepositoryAccessException("Employee with username " + username + " not found");
            }
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        }
    }

    @Override
    public List<T> findAll() throws RepositoryAccessException {
        try (Connection connection = Database.connectToDatabase();
             Statement sqlStatement = connection.createStatement()) {
            List<T> employeeList = new ArrayList<>();
            ResultSet resultSet = sqlStatement.executeQuery("SELECT NAME, USERNAME, EMAIL, PHONE FROM EMPLOYEE");
            while (resultSet.next()) {
                Employee employee = getEmployeeFromResultSet(resultSet);
                employeeList.add((T)employee);
            }
            return employeeList;
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        }
    }
    /**
     * Takes a ResultSet object and returns an Employee object created from the data in the ResultSet.
     * @param rs The ResultSet object containing the data to create the Employee object.
     * @return The Employee object created from the data in the ResultSet.
     * @throws SQLException If an error occurs while accessing the ResultSet object.
     */
    private static Employee getEmployeeFromResultSet(ResultSet rs) throws SQLException {
        String name = rs.getString("NAME");
        String username = rs.getString("USERNAME");
        String email = rs.getString("EMAIL");
        String phone = rs.getString("PHONE");
        return new Employee(name, username, email, phone);
    }

    @Override
    public synchronized void save(List<T> users) {
        try (Connection connection = Database.connectToDatabase();
             PreparedStatement sqlStatement = connection.prepareStatement(
                     "INSERT INTO EMPLOYEE (NAME, USERNAME, EMAIL, PHONE) VALUES (?, ?, ?, ?)")) {
            for (T user : users) {
                sqlStatement.setString(1, user.getName());
                sqlStatement.setString(2, user.getUsername());
                sqlStatement.setString(3, user.getEmail());
                sqlStatement.setString(4, user.getPhone());
                sqlStatement.addBatch();
            }
            sqlStatement.executeBatch();
        } catch (IOException | SQLException e) {
            throw new RepositoryAccessException(e);
        }
    }
    /**
     * Updates an Employee object in the database. The oldUsername parameter is used to identify the Employee object to
     * update. Updates the EMPLOYEE table and the GIVEN_TO column in the BUDGET table along with the
     * BY column in the EXPENSE table.
     * @param employee The Employee object to update.
     * @param oldUsername The username of the Employee object to be updated.
     */
    public void update(Employee employee, String oldUsername) {
        try (Connection connection = Database.connectToDatabase()){
             Statement disableIntegrityStatement = connection.createStatement();
            disableIntegrityStatement.execute("SET REFERENTIAL_INTEGRITY FALSE");

             PreparedStatement updateStatementEmployeeTable = connection.prepareStatement(
                     "UPDATE EMPLOYEE SET NAME = ?, USERNAME = ?, EMAIL = ?, PHONE = ? WHERE USERNAME = ?");
            updateStatementEmployeeTable.setString(1, employee.getName());
            updateStatementEmployeeTable.setString(2, employee.getUsername());
            updateStatementEmployeeTable.setString(3, employee.getEmail());
            updateStatementEmployeeTable.setString(4, employee.getPhone());
            updateStatementEmployeeTable.setString(5, oldUsername);
            updateStatementEmployeeTable.executeUpdate();

            PreparedStatement updateStatementBudgetTable = connection.prepareStatement(
                    "UPDATE BUDGET SET GIVEN_TO = ? WHERE GIVEN_TO = ?");
            updateStatementBudgetTable.setString(1, employee.getUsername());
            updateStatementBudgetTable.setString(2, oldUsername);
            updateStatementBudgetTable.executeUpdate();

            PreparedStatement updateStatementExpenseTable = connection.prepareStatement(
                    "UPDATE EXPENSE SET BY = ? WHERE BY = ?");
            updateStatementExpenseTable.setString(1, employee.getUsername());
            updateStatementExpenseTable.setString(2, oldUsername);
            updateStatementExpenseTable.executeUpdate();

            Statement enableIntegrityStatement = connection.createStatement();
            enableIntegrityStatement.execute("SET REFERENTIAL_INTEGRITY TRUE");
        } catch (SQLException | IOException e) {
            throw new RepositoryAccessException(e);
        }
    }
    /**
     * Deletes an Employee object from the database. Deletes the Employee object from the EMPLOYEE table, the BUDGET table
     * and the EXPENSE table, using the username of the Employee object to identify it.
     * @param username The username of the Employee object to delete.
     */
    public void delete(String username) {
        try (Connection connection = Database.connectToDatabase()) {
            PreparedStatement deleteStatementExpenseTable = connection.prepareStatement(
                    "DELETE FROM EXPENSE WHERE BY = ?");
            deleteStatementExpenseTable.setString(1, username);
            deleteStatementExpenseTable.executeUpdate();

            PreparedStatement deleteStatementBudgetTable = connection.prepareStatement(
                    "DELETE FROM BUDGET WHERE GIVEN_TO = ?");
            deleteStatementBudgetTable.setString(1, username);
            deleteStatementBudgetTable.executeUpdate();

            PreparedStatement deleteStatementEmployeeTable = connection.prepareStatement(
                    "DELETE FROM EMPLOYEE WHERE USERNAME = ?");
            deleteStatementEmployeeTable.setString(1, username);
            deleteStatementEmployeeTable.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RepositoryAccessException(e);
        }
    }

    @Override
    public void save(T user) {
        List<T> employees = new ArrayList<>();
        employees.add(user);
        save(employees);
    }
}