package com.verify_x.exception;

public class GeocodingException extends RuntimeException {

    public GeocodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeocodingException(String message) {
        super(message);
    }
}