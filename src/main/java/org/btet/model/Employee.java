package org.btet.model;

import org.btet.enums.UserRole;
/**
 * Employee class is a subclass of User class, and it represents an employee in the system.
 * Each employee has a name, username, email and phone number.
 * */
public class Employee extends User{
    private String email;
    private String phone;
    /**
     * Constructor for Employee class.
     * @param name name of the employee
     * @param username username of the employee
     * @param email email of the employee
     * @param phone phone number of the employee
     * */
    public Employee(String name, String username,String email, String phone){
        super(name, UserRole.EMPLOYEE, username);
        this.email = email;
        this.phone = phone;
    }
    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
