package com.blingbag.clone.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        
        ModelAndView mav = new ModelAndView("error/404");
        Map<String, Object> error = getErrorAttributes(ex, request, HttpStatus.NOT_FOUND);
        mav.addAllObjects(error);
        return mav;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.error("Invalid request: {}", ex.getMessage());
        
        ModelAndView mav = new ModelAndView("error/400");
        Map<String, Object> error = getErrorAttributes(ex, request, HttpStatus.BAD_REQUEST);
        mav.addAllObjects(error);
        return mav;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        
        ModelAndView mav = new ModelAndView("error/403");
        Map<String, Object> error = getErrorAttributes(ex, request, HttpStatus.FORBIDDEN);
        mav.addAllObjects(error);
        return mav;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        log.error("Internal server error: ", ex);
        
        ModelAndView mav = new ModelAndView("error/500");
        Map<String, Object> error = getErrorAttributes(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addAllObjects(error);
        return mav;
    }

    // Custom exceptions
    @ExceptionHandler(OutOfStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleOutOfStockException(OutOfStockException ex, Model model) {
        log.error("Out of stock error: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/stock-error";
    }

    @ExceptionHandler(PaymentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handlePaymentException(PaymentException ex, Model model) {
        log.error("Payment error: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/payment-error";
    }

    @ExceptionHandler(OrderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleOrderException(OrderException ex, Model model) {
        log.error("Order error: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/order-error";
    }

    // Helper methods
    private Map<String, Object> getErrorAttributes(Exception ex, HttpServletRequest request, HttpStatus status) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        errorAttributes.put("timestamp", LocalDateTime.now());
        errorAttributes.put("status", status.value());
        errorAttributes.put("error", status.getReasonPhrase());
        errorAttributes.put("message", ex.getMessage());
        errorAttributes.put("path", request.getRequestURI());
        
        // Add additional information for development environment
        if (isDevelopment()) {
            errorAttributes.put("exception", ex.getClass().getName());
            errorAttributes.put("trace", ex.getStackTrace());
        }
        
        return errorAttributes;
    }

    private boolean isDevelopment() {
        // Check if we're running in development mode
        // This could be based on a profile or environment variable
        return true; // For demonstration, always return true
    }
}

// Custom exception classes
@Slf4j
class OutOfStockException extends RuntimeException {
    public OutOfStockException(String message) {
        super(message);
        log.error("OutOfStockException: {}", message);
    }
}

@Slf4j
class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
        log.error("PaymentException: {}", message);
    }
}

@Slf4j
class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
        log.error("OrderException: {}", message);
    }
}
