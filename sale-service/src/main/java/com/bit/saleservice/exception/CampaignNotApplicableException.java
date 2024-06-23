package com.bit.saleservice.exception;

/**
 * This exception is thrown when a campaign is not applicable for a specific sale.
 * It extends RuntimeException to allow for unchecked exceptions.
 */
public class CampaignNotApplicableException extends RuntimeException {

    /**
     * Constructs a new CampaignNotApplicableException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public CampaignNotApplicableException(String message) {
        super(message);
    }
}
