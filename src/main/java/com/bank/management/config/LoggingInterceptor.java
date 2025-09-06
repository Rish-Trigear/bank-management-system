package com.bank.management.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * HTTP Request/Response logging interceptor
 * 
 * This interceptor logs all incoming HTTP requests and outgoing responses
 * including method, URI, status codes, and any exceptions that occur.
 * Used for monitoring and debugging API calls.
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    /**
     * Called before the request is handled
     * Logs incoming request details
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logger.info("Request: {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        return true; // Continue with request processing
    }

    /**
     * Called after request is completed
     * Logs response status and any exceptions
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        logger.info("Response: {} {} - Status: {}", request.getMethod(), request.getRequestURI(), response.getStatus());
        if (ex != null) {
            logger.error("Request completed with exception: {}", ex.getMessage());
        }
    }
}