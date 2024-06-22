package com.bit.productservice.service;

import com.bit.productservice.exception.AlgorithmNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Log4j2
@Service
public class BarcodeService {

    @Value("${company.name}")
    private String COMPANY_NAME;

    public String generateBarcodeNumber(String productName) throws AlgorithmNotFoundException {
        log.trace("Entering generateBarcodeNumber method in BarcodeService class");
        log.debug("Generating barcode for product: {}", productName);

        String combinedName = COMPANY_NAME + productName;
        log.debug("Combined company name and product name: {}", combinedName);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            log.debug("SHA-256 MessageDigest instance created");

            byte[] hashBytes = digest.digest(combinedName.getBytes());
            log.debug("Hashed bytes: {}", hashBytes);

            long numericValue = 0;
            for (byte b : hashBytes) {
                numericValue = (numericValue << 8) | (b & 0xFF);
            }
            log.debug("Numeric value derived from hash: {}", numericValue);

            long companyValue = Long.parseLong(COMPANY_NAME.replaceAll("\\D+", ""));
            log.debug("Numeric value of company name: {}", companyValue);

            long finalValue = (companyValue * 10000000000L) + (numericValue % 10000000000L);
            log.debug("Final value before adjustment: {}", finalValue);

            long firstDigit = finalValue / 1000000000L;
            if (firstDigit == 0) {
                finalValue += 1000000000L;
                log.debug("Adjusted final value to ensure first digit is non-zero: {}", finalValue);
            }

            String barcode = String.format("%013d", Math.abs(finalValue));
            log.info("Generated barcode for product: {}, {} ", productName, barcode);

            log.trace("Exiting generateBarcodeNumber method in BarcodeService class");
            return barcode;

        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            throw new AlgorithmNotFoundException("SHA-256 algorithm not found", e);
        }
    }
}