package org.btet.model;

import org.btet.enums.UserRole;
/**
 * The Manager class represents a manager user in the system, it extends the User class.
 * Each manager has a username, name, department and role.
 * The department is the department the manager is responsible for.
 */
public class Manager extends User{
    private String department;
    /**
     * Constructor for the Manager class.
     * @param name The name of the manager.
     * @param username The username of the manager.
     * @param department The department the manager is responsible for.
     */
    public Manager(String name, String username, String department) {
        super(name, UserRole.MANAGER, username);
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
