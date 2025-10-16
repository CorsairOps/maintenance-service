package com.corsairops.maintenance.exception;

import com.corsairops.shared.exception.HttpResponseException;
import org.springframework.http.HttpStatus;

public class OrderNoteNotFoundException extends HttpResponseException {
    public OrderNoteNotFoundException(String message, HttpStatus status) {
        super(message, status);
    }
}