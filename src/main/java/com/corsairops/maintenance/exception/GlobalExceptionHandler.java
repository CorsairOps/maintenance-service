package com.corsairops.maintenance.exception;

import com.corsairops.shared.exception.SimpleGlobalExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends SimpleGlobalExceptionHandler {
}