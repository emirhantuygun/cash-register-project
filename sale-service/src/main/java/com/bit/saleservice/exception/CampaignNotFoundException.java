package com.bit.saleservice.exception;

/**
 * This class represents a custom exception that is thrown when a campaign is not found.
 * It extends the RuntimeException class, which means it does not require explicit catching.
 */
public class CampaignNotFoundException extends RuntimeException {

    /**
     * Constructs a new CampaignNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public CampaignNotFoundException(String message) {
        super(message);
    }
}
