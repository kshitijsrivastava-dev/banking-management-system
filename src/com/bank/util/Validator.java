package com.bank.util;

import com.bank.exception.InvalidInputException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public final class Validator {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern PIN = Pattern.compile("^\\d{4}$");

    private Validator() {}

    public static void validateName(String name) throws InvalidInputException {
        if (name == null || name.trim().length() < 2)
            throw new InvalidInputException("Name must be at least 2 characters.");
    }

    public static void validateEmail(String email) throws InvalidInputException {
        if (email == null || !EMAIL.matcher(email.trim()).matches())
            throw new InvalidInputException("Invalid email address.");
    }

    public static void validatePhone(String phone) throws InvalidInputException {
        if (phone == null || !PHONE.matcher(phone.trim()).matches())
            throw new InvalidInputException("Phone must be a valid 10-digit Indian mobile number.");
    }

    public static void validatePin(String pin) throws InvalidInputException {
        if (pin == null || !PIN.matcher(pin).matches())
            throw new InvalidInputException("PIN must be exactly 4 digits.");
    }

    public static void validateAmount(BigDecimal amount) throws InvalidInputException {
        if (amount == null || amount.signum() <= 0)
            throw new InvalidInputException("Amount must be greater than zero.");
        if (amount.scale() > 2)
            throw new InvalidInputException("Amount can have at most 2 decimal places.");
    }

    public static String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(pin.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
