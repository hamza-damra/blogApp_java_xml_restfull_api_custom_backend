package com.hamza.blogapp_custombackend.validations;

public class Validation {

    private static boolean isValidEmail(String email) {
        String emailPattern = "^(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";
        return email.matches(emailPattern);
    }

    public static boolean validateLength(String input, int minLength, int maxLength)
    {
        return input.length() >= minLength && input.length() <= maxLength;
    }

    public static boolean validateEmail(String email)
    {
        return isValidEmail(email) && !email.isEmpty();
    }

    public static boolean validateNotEmpty(String input) {
        return !input.isEmpty();
    }

}
