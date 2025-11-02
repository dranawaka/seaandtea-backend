package com.seaandtea.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JsonLoggingUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    /**
     * Log request data in JSON format
     */
    public static void logRequestJson(String endpoint, String method, Object body, Map<String, String> headers, Map<String, String> params) {
        try {
            Map<String, Object> requestLog = new HashMap<>();
            requestLog.put("timestamp", LocalDateTime.now());
            requestLog.put("type", "REQUEST");
            requestLog.put("endpoint", endpoint);
            requestLog.put("method", method);
            requestLog.put("body", body);
            requestLog.put("headers", headers);
            requestLog.put("parameters", params);
            
            String jsonLog = objectMapper.writeValueAsString(requestLog);
            log.info("=== REQUEST JSON ===\n{}", jsonLog);
            
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize request to JSON: {}", e.getMessage());
            // Fallback to regular logging
            log.info("=== REQUEST (JSON serialization failed) ===");
            log.info("Endpoint: {} {}", method, endpoint);
            log.info("Body: {}", body);
            log.info("Headers: {}", headers);
            log.info("Parameters: {}", params);
        }
    }
    
    /**
     * Log comprehensive request data in JSON format
     */
    public static void logComprehensiveRequestJson(Map<String, Object> requestData) {
        try {
            String jsonLog = objectMapper.writeValueAsString(requestData);
            log.info("=== COMPREHENSIVE REQUEST JSON ===\n{}", jsonLog);
            
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize comprehensive request to JSON: {}", e.getMessage());
            // Fallback to regular logging
            log.info("=== COMPREHENSIVE REQUEST (JSON serialization failed) ===");
            log.info("Request Data: {}", requestData);
        }
    }
    
    /**
     * Log response data in JSON format
     */
    public static void logResponseJson(String endpoint, String method, int statusCode, Object body, long responseTime) {
        try {
            Map<String, Object> responseLog = new HashMap<>();
            responseLog.put("timestamp", LocalDateTime.now());
            responseLog.put("type", "RESPONSE");
            responseLog.put("endpoint", endpoint);
            responseLog.put("method", method);
            responseLog.put("statusCode", statusCode);
            responseLog.put("body", body);
            responseLog.put("responseTimeMs", responseTime);
            
            String jsonLog = objectMapper.writeValueAsString(responseLog);
            log.info("=== RESPONSE JSON ===\n{}", jsonLog);
            
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize response to JSON: {}", e.getMessage());
            // Fallback to regular logging
            log.info("=== RESPONSE (JSON serialization failed) ===");
            log.info("Endpoint: {} {}", method, endpoint);
            log.info("Status: {}", statusCode);
            log.info("Body: {}", body);
            log.info("Response Time: {}ms", responseTime);
        }
    }
    
    /**
     * Log comprehensive response data in JSON format
     */
    public static void logComprehensiveResponseJson(Map<String, Object> responseData) {
        try {
            String jsonLog = objectMapper.writeValueAsString(responseData);
            log.info("=== COMPREHENSIVE RESPONSE JSON ===\n{}", jsonLog);
            
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize comprehensive response to JSON: {}", e.getMessage());
            // Fallback to regular logging
            log.info("=== COMPREHENSIVE RESPONSE (JSON serialization failed) ===");
            log.info("Response Data: {}", responseData);
        }
    }
    
    /**
     * Log error data in JSON format
     */
    public static void logErrorJson(String endpoint, String method, Exception error, int statusCode) {
        try {
            Map<String, Object> errorLog = new HashMap<>();
            errorLog.put("timestamp", LocalDateTime.now());
            errorLog.put("type", "ERROR");
            errorLog.put("endpoint", endpoint);
            errorLog.put("method", method);
            errorLog.put("statusCode", statusCode);
            errorLog.put("errorType", error.getClass().getSimpleName());
            errorLog.put("errorMessage", error.getMessage());
            errorLog.put("stackTrace", getStackTrace(error));
            
            String jsonLog = objectMapper.writeValueAsString(errorLog);
            log.error("=== ERROR JSON ===\n{}", jsonLog);
            
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize error to JSON: {}", e.getMessage());
            // Fallback to regular logging
            log.error("=== ERROR (JSON serialization failed) ===");
            log.error("Endpoint: {} {}", method, endpoint);
            log.error("Status: {}", statusCode);
            log.error("Error: {}", error.getMessage(), error);
        }
    }
    
    /**
     * Get stack trace as string array (first 10 lines)
     */
    private static String[] getStackTrace(Exception error) {
        StackTraceElement[] stackTrace = error.getStackTrace();
        int maxLines = Math.min(10, stackTrace.length);
        String[] trace = new String[maxLines];
        
        for (int i = 0; i < maxLines; i++) {
            trace[i] = stackTrace[i].toString();
        }
        
        return trace;
    }
    
    /**
     * Convert object to JSON string for logging
     */
    public static String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "JSON serialization failed: " + e.getMessage();
        }
    }
}
