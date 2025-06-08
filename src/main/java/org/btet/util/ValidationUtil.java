package org.btet.util;

import org.btet.exception.FormatEmailPhoneException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.math.BigDecimal;
/**
 * The ValidationUtil class is used to validate inputs by the user so that they are in the correct format,
 * by providing static methods.
 * */
public class ValidationUtil {
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtil.class.getName());
    /**
     * Validates the email and phone number format by using regex-patterns.
     * @param email - email to be validated
     * @param phone - phone number to be validated
     * @throws FormatEmailPhoneException - if the email or phone number is not in the correct format
     * */
    public static void validateEmailAndPhone(String email, String phone) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new FormatEmailPhoneException("Invalid email format");
        }
        if (!phone.matches("^\\+385\\d{9}$")) {
            throw new FormatEmailPhoneException("Invalid phone format: +385XXXXXXXX");
        }
    }
    /**
     * Parses the string value to a BigDecimal object.
     * @param value - value to be parsed
     * @return BigDecimal object if the value is not null or empty, otherwise null
     * */
    public static BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            logger.info("Error parsing BigDecimal: {}", e.getMessage());
            return null;
        }
    }
    private ValidationUtil(){}
}
