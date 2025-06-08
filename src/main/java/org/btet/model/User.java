package org.btet.model;

import org.btet.enums.UserRole;
/**
 * The user class is the parent class for all the users in the system.
 * It contains the common attributes and methods that are shared by all the users.
 * The class is abstract, and each user has a name, role and username which they are identified by.
 */
public abstract class User {
    private String name;
    private UserRole role;
    private String username;
    /**
     *The constructor for the User class.
     * @param name The name of the user
     * @param role The role of the user
     * @param username The username of the user
     * */
    protected User(String name, UserRole role, String username) {
        this.name = name;
        this.role = role;
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return role + ": " + name;
    }
}
