package org.btet.util;

import org.btet.enums.UserRole;
import org.btet.exception.InvalidLoginCredentialsException;
import org.btet.exception.LoadUsersException;
import org.btet.exception.PasswordHashAlgorithmException;
import org.btet.model.UserLoginRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
/**
 * The LoginUtil class is a utility class that provides static methods for user login, logout, user management,
 * password hashing, and password updating. The class also provides a method for loading users from a file and
 * writing users to a file. The class uses a map to store user passwords and roles.
 * */
public class LoginUtil {
    private static final String USER_FILE = "dat/users.txt";
    private static  Map<String, String> userPasswordMap = new HashMap<>();
    private static  Map<String, UserRole> userRoleMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(LoginUtil.class);
    private static UserLoginRecord loggedUserRecord = null;
    /**
     * Takes a password as a parameter and returns a hashed password using the SHA-256 algorithm.
     * @param password The password to be hashed.
     * @throws PasswordHashAlgorithmException If the hashing algorithm is not found.
     * @return The hashed password.
     * */
    public static String hashPassword(String password) throws PasswordHashAlgorithmException{
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashArray = messageDigest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashArray) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new PasswordHashAlgorithmException(e);
        }
    }
    /**
     * Reads user data from a file and populates the userPasswordMap and userRoleMap maps.
     * @throws LoadUsersException If an error occurs while reading the file.
     * */
    private static void loadUsers() throws LoadUsersException {
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                userPasswordMap.put(parts[0], parts[1]);
                userRoleMap.put(parts[0], UserRole.valueOf(parts[2]));
            }
        } catch (IOException e) {
            throw new LoadUsersException(e);
        }
    }

    /**
     * Takes a username and password as parameters and checks if the username and password match the data in the userPasswordMap.
     * If the username and password match, the method returns a UserLoginRecord object with the username and role.
     * If the username and password do not match, the method throws an InvalidLoginCredentialsException.
     * @param username The username to be checked.
     * @param password The password to be checked.
     * @throws InvalidLoginCredentialsException If the username and password do not match.
     * @return A UserLoginRecord object with the username and role.
     * */
    public static UserLoginRecord login (String username, String password){
        try{
            loadUsers();
        }catch (LoadUsersException e){
            logger.info("Error loading users in login() method: {}", e.getMessage());
        }
        String hashedPassword = "";
        try {
            hashedPassword = hashPassword(password);
        }catch (PasswordHashAlgorithmException e){
            logger.info("Error hashing password: {}", e.getMessage());
        }

        if (userPasswordMap.containsKey(username) && userPasswordMap.get(username).contentEquals(hashedPassword)) {
            loggedUserRecord = new UserLoginRecord(username, userRoleMap.get(username));
            return new UserLoginRecord(username, userRoleMap.get(username));
        }
        else{
            throw new InvalidLoginCredentialsException("Invalid username or password");
        }
    }
    /**
     * Writes the user data from the userPasswordMap and userRoleMap maps to a file.
     * */
    private static void writeUsers() {
        try(PrintWriter pr = new PrintWriter(USER_FILE)){
            for (Map.Entry<String, String> entry : userPasswordMap.entrySet()) {
                pr.println(entry.getKey() + "," + entry.getValue() + "," + userRoleMap.get(entry.getKey()));
            }
        } catch (IOException e) {
            logger.info("Error writing users to file: {}", e.getMessage());
        }
    }
    /**
     * Updates the username in the userPasswordMap and userRoleMap maps, and writes the updated data to a file
     * (overwriting the existing data).
     * @param toBeChangedUsername The username to be changed.
     * @param newUsername The new username.
     * */
    public static void updateUsername(String toBeChangedUsername, String newUsername){
        try{
            loadUsers();
        }catch (LoadUsersException e){
            logger.info("Error loading users: {}", e.getMessage());
        }
        if(userPasswordMap.containsKey(toBeChangedUsername)){
            String password = userPasswordMap.get(toBeChangedUsername);
            UserRole role = userRoleMap.get(toBeChangedUsername);
            userPasswordMap.remove(toBeChangedUsername);
            userRoleMap.remove(toBeChangedUsername);
            userPasswordMap.put(newUsername, password);
            userRoleMap.put(newUsername, role);
            writeUsers();
        }
    }
    /**
     * Updates the password in the userPasswordMap and writes the updated data to a file (overwriting the existing data).
     * @param username The username for which the password is to be updated.
     * @param newPassword The new password.
     * */
    public static void updatePassword(String username, String newPassword){
        try{
            loadUsers();
        }catch (LoadUsersException e){
            logger.info("Error loading users in updatePassword: {}", e.getMessage());
        }
        try {
            userPasswordMap.remove(username);
            userPasswordMap.put(username, hashPassword(newPassword));
        } catch (PasswordHashAlgorithmException e) {
            logger.info("Error hashing password in updatePassword method: {}", e.getMessage());
        }
        writeUsers();
    }
    /**
     * Adds a new user to the userPasswordMap and userRoleMap maps, and writes the updated data to a file
     * (overwriting the existing data).
     * @param username The username of the new user.
     * @param password The password of the new user.
     * @param role The role of the new user.
     * */
    public static void addNewUser(String username, String password, UserRole role){
        try{
            loadUsers();
        }catch (LoadUsersException e){
            logger.info("Error loading users in addNewUser method: {}", e.getMessage());
        }

        try {
            userPasswordMap.put(username, hashPassword(password));
        } catch (PasswordHashAlgorithmException e) {
            logger.info("Error hashing password in addNewUser method: {}", e.getMessage());
        }
        userRoleMap.put(username, role);
        writeUsers();
    }
    /**
     * Removes a user from the userPasswordMap and userRoleMap maps, and writes the updated data to a file
     * (overwriting the existing data).
     * @param username The username of the user to be removed.
     * */
    public static void removeUser(String username){
        try{
            loadUsers();
        }catch (LoadUsersException e){
            logger.info("Error loading users in removeUser method: {}", e.getMessage());
        }
        userPasswordMap.remove(username);
        userRoleMap.remove(username);
        writeUsers();
    }
    /**
     * Returns the UserLoginRecord object of the currently logged-in user.
     * @return The UserLoginRecord object.
     * */
    public static UserLoginRecord getLoggedUserRecord() {
        return loggedUserRecord;
    }
    /**
     * Returns the userPasswordMap map.
     * @return a Map of string keys and string values.
     * */
    public static Map<String, String> getUserPasswordMap() {
        return userPasswordMap;
    }

    private LoginUtil(){}

    /**
     * Sets the loggedUserRecord to null, used for logging out.
     * */
    public static void logout() {
        loggedUserRecord = null;
    }
}
