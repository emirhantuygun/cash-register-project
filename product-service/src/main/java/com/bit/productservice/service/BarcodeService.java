package com.bit.productservice.service;

import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class BarcodeService {

    private final String COMPANY_NAME = "32bit";

    public String generateBarcodeNumber(String productName) {
        String combinedName = COMPANY_NAME + productName;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combinedName.getBytes());

            // Convert the hash bytes to a numeric value
            long numericValue = 0;
            for (byte b : hashBytes) {
                numericValue = (numericValue << 8) | (b & 0xFF);
            }

            // Convert the first digits of the company name to a numeric value
            long companyValue = Long.parseLong(COMPANY_NAME.replaceAll("\\D+", ""));

            // Merge the company value and the hash value
            long finalValue = (companyValue * 10000000000L) + (numericValue % 10000000000L);

            // Ensure the first digit is not zero
            long firstDigit = finalValue / 1000000000L;
            if (firstDigit == 0) {
                finalValue += 1000000000L; // Add 1 billion to shift the first digit
            }

            // Format the final value to ensure it has 13 digits
            return String.format("%013d", Math.abs(finalValue));

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
