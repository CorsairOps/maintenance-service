package com.corsairops.maintenance.exception;

import com.corsairops.shared.exception.HttpResponseException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends HttpResponseException {
    public OrderNotFoundException(String message, HttpStatus status) {
        super(message, status);
    }
}