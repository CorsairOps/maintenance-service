package com.corsairops.maintenance.exception;

import com.corsairops.shared.exception.HttpResponseException;
import org.springframework.http.HttpStatus;

public class OpenOrderExistsException extends HttpResponseException {
    public OpenOrderExistsException(String message, HttpStatus status) {
        super(message, status);
    }
}