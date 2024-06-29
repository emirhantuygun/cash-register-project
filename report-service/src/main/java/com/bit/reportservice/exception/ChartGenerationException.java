package com.bit.reportservice.exception;

/**
 * Custom exception class for handling chart generation errors.
 * This class extends RuntimeException to allow for unchecked exceptions.
 */
public class ChartGenerationException extends RuntimeException  {

    /**
     * Constructs a new ChartGenerationException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public ChartGenerationException(String message) {
        super(message);
    }
}
