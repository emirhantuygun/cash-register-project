package com.bit.saleservice.exception;

/**
 * Custom exception class for handling duplicate campaigns.
 * This exception is thrown when a campaign with the same name already exists in the system.
 */
public class DuplicateCampaignException extends RuntimeException {

    /**
     * Constructs a new DuplicateCampaignException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public DuplicateCampaignException(String message) {
        super(message);
    }
}
