package org.btet.controller;

import org.btet.util.LoginUtil;
import org.btet.util.ScreenManager;
/**
 * This class is the controller for the AdminMenuBar.fxml file.
 * Contains methods that are called when the user clicks on the menu items, for navigating through the admin screens.
 */
public class AdminMenuBarController {
    /**
     * Logs out the user and redirects to the login screen.
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
     * Opens the screen for searching and updating the employee details.
     */
    public void openEmployeeSearch() {
        ScreenManager.showScreen("/fxml/AdminEmployeeSearchUpdate.fxml");
    }
    /**
     * Opens the screen for searching and updating the manager details.
     */
    public void openManagerSearch() {
        ScreenManager.showScreen("/fxml/AdminManagerSearchUpdate.fxml");
    }
    /**
     * Opens the home screen for the admin, where the admin can view AuditLogs;
     */
    public void openHome() {
        ScreenManager.showScreen("/fxml/AdminHome.fxml");
    }
    /**
     * Opens the screen for adding a new employee.
     */
    public void openAddEmployee() {
        ScreenManager.showScreen("/fxml/AdminAddEmployee.fxml");
    }
    /**
     * Opens the screen for adding a new manager.
     */
    public void openAddManager() {
        ScreenManager.showScreen("/fxml/AdminAddManager.fxml");
    }
    /**
     * Opens the screen for searching and updating the expense details.
     */
    public void openExpenseSearch() {
        ScreenManager.showScreen("/fxml/AdminExpenseSearchUpdate.fxml");
    }
    /**
     * Opens the screen for searching and updating the budget details.
     */
    public void openBudgetSearch() {
        ScreenManager.showScreen("/fxml/AdminBudgetSearchUpdate.fxml");
    }
}
