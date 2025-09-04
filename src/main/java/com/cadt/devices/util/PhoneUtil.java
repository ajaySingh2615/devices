package com.cadt.devices.util;

/**
 * Utility class for phone number normalization
 * Ensures consistent phone number format across the system
 */
public class PhoneUtil {
    
    /**
     * Normalizes phone number to international format
     * @param phone Raw phone number input
     * @return Normalized phone number (+country code + digits only), or null if input is null/empty
     */
    public static String normalizePhone(String phone) {
        // Handle null or empty input
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        // Remove all non-digits except the leading +
        String cleaned = phone.trim().replaceAll("[^\\d+]", "");
        
        // If it's empty after cleaning, return null
        if (cleaned.isEmpty()) {
            return null;
        }
        
        // If it already starts with +, validate and return
        if (cleaned.startsWith("+")) {
            if (cleaned.matches("^\\+[1-9]\\d{7,14}$")) {
                return cleaned;
            } else {
                // Log the error but return null instead of throwing exception
                System.err.println("⚠️ [PhoneUtil] Invalid international phone format: " + phone + " -> " + cleaned);
                return null;
            }
        }
        
        // Auto-add +91 for 10-digit Indian numbers
        if (cleaned.matches("^\\d{10}$")) {
            System.out.println("🇮🇳 [PhoneUtil] Auto-adding +91 to: " + cleaned + " -> +91" + cleaned);
            return "+91" + cleaned;
        }
        
        // For other lengths, log warning and return null
        if (cleaned.matches("^\\d{7,14}$")) {
            System.err.println("⚠️ [PhoneUtil] Phone needs country code: " + phone + " -> " + cleaned);
            return null;
        }
        
        // Log invalid format and return null
        System.err.println("⚠️ [PhoneUtil] Invalid phone format: " + phone + " -> " + cleaned);
        return null;
    }
    
    /**
     * Checks if two phone numbers are the same after normalization
     * @param phone1 First phone number
     * @param phone2 Second phone number
     * @return true if they represent the same phone number
     */
    public static boolean isSamePhone(String phone1, String phone2) {
        try {
            String normalized1 = normalizePhone(phone1);
            String normalized2 = normalizePhone(phone2);
            return normalized1 != null && normalized1.equals(normalized2);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
