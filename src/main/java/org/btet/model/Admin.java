package org.btet.model;

import org.btet.enums.UserRole;
/**
 * Admin class that extends the User abstract class and sets the role of the user to ADMIN, representing an admin user.
 * Each admin user has a name and a username.
 */
public class Admin extends User {
    /**
     * Constructor for the Admin class.
     * @param name The name of the admin user.
     * @param username The username of the admin user.
     */
    public Admin(String name, String username) {
        super(name, UserRole.ADMIN, username);
    }
}
