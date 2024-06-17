package com.bit.productservice.service;

import com.bit.productservice.exception.AlgorithmNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class BarcodeService {

    @Value("${company.name}")
    private String COMPANY_NAME;

    public String generateBarcodeNumber(String productName) throws AlgorithmNotFoundException {
        String combinedName = COMPANY_NAME + productName;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combinedName.getBytes());

            long numericValue = 0;
            for (byte b : hashBytes) {
                numericValue = (numericValue << 8) | (b & 0xFF);
            }

            long companyValue = Long.parseLong(COMPANY_NAME.replaceAll("\\D+", ""));

            long finalValue = (companyValue * 10000000000L) + (numericValue % 10000000000L);

            long firstDigit = finalValue / 1000000000L;
            if (firstDigit == 0) {
                finalValue += 1000000000L;
            }
            return String.format("%013d", Math.abs(finalValue));

        } catch (NoSuchAlgorithmException e) {
            throw new AlgorithmNotFoundException("SHA-256 algorithm not found", e);
        }
    }
}
