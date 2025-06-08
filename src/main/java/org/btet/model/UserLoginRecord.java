package org.btet.model;

import org.btet.enums.UserRole;
/**
 * A record class that represents a user login record, is used when logging in the application, so
 * that the user can be identified by their username and role, and used throughout the application by accessing the
 * created record in the {@link org.btet.util.LoginUtil} class.
 * */
public record UserLoginRecord(String username, UserRole role) {
}
