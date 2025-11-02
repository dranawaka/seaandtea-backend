package com.seaandtea.interceptor;

import com.seaandtea.util.JsonLoggingUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class RequestResponseLoggingInterceptor implements HandlerInterceptor {
    
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String START_TIME_ATTRIBUTE = "requestStartTime";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Generate unique request ID
        String requestId = UUID.randomUUID().toString();
        request.setAttribute("requestId", requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        
        // Record start time
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        
        // Extract headers
        Map<String, String> headers = extractHeaders(request);
        
        // Extract parameters
        Map<String, String> params = extractParameters(request);
        
        // Log incoming request
        logIncomingRequest(request, headers, params, requestId);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Get request ID and start time
        String requestId = (String) request.getAttribute("requestId");
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        
        if (startTime != null) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Log outgoing response
            logOutgoingResponse(request, response, responseTime, requestId);
        }
    }
    
    private void logIncomingRequest(HttpServletRequest request, Map<String, String> headers, Map<String, String> params, String requestId) {
        try {
            // Create request log data
            Map<String, Object> requestLog = new HashMap<>();
            requestLog.put("requestId", requestId);
            requestLog.put("timestamp", System.currentTimeMillis());
            requestLog.put("type", "INCOMING_REQUEST");
            requestLog.put("method", request.getMethod());
            requestLog.put("uri", request.getRequestURI());
            requestLog.put("fullUrl", getFullUrl(request));
            requestLog.put("remoteAddress", request.getRemoteAddr());
            requestLog.put("remoteHost", request.getRemoteHost());
            requestLog.put("userAgent", request.getHeader("User-Agent"));
            requestLog.put("contentType", request.getContentType());
            requestLog.put("contentLength", request.getContentLength());
            requestLog.put("headers", headers);
            requestLog.put("parameters", params);
            requestLog.put("queryString", request.getQueryString());
            
            // Log request body if available
            if (request instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
                String requestBody = getRequestBody(wrapper);
                if (requestBody != null && !requestBody.isEmpty()) {
                    requestLog.put("body", requestBody);
                }
            }
            
            // Use comprehensive JSON logging utility
            JsonLoggingUtil.logComprehensiveRequestJson(requestLog);
            
            // Also log additional details
            log.info("=== INCOMING REQUEST [{}] ===", requestId);
            log.info("Method: {} {}", request.getMethod(), request.getRequestURI());
            log.info("Remote: {} ({})", request.getRemoteAddr(), request.getRemoteHost());
            log.info("Content-Type: {}, Length: {}", request.getContentType(), request.getContentLength());
            log.info("User-Agent: {}", request.getHeader("User-Agent"));
            
        } catch (Exception e) {
            log.warn("Failed to log incoming request: {}", e.getMessage());
        }
    }
    
    private void logOutgoingResponse(HttpServletRequest request, HttpServletResponse response, long responseTime, String requestId) {
        try {
            // Create response log data
            Map<String, Object> responseLog = new HashMap<>();
            responseLog.put("requestId", requestId);
            responseLog.put("timestamp", System.currentTimeMillis());
            responseLog.put("type", "OUTGOING_RESPONSE");
            responseLog.put("method", request.getMethod());
            responseLog.put("uri", request.getRequestURI());
            responseLog.put("statusCode", response.getStatus());
            responseLog.put("statusText", getStatusText(response.getStatus()));
            responseLog.put("responseTimeMs", responseTime);
            responseLog.put("contentType", response.getContentType());
            responseLog.put("contentLength", response.getBufferSize());
            
            // Extract response headers
            Map<String, String> responseHeaders = new HashMap<>();
            for (String headerName : response.getHeaderNames()) {
                responseHeaders.put(headerName, response.getHeader(headerName));
            }
            responseLog.put("responseHeaders", responseHeaders);
            
            // Log response body if available
            if (response instanceof ContentCachingResponseWrapper) {
                ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
                String responseBody = getResponseBody(wrapper);
                if (responseBody != null && !responseBody.isEmpty()) {
                    responseLog.put("body", responseBody);
                }
            }
            
            // Use comprehensive JSON logging utility
            JsonLoggingUtil.logComprehensiveResponseJson(responseLog);
            
            // Also log additional details
            log.info("=== OUTGOING RESPONSE [{}] ===", requestId);
            log.info("Status: {} ({})", response.getStatus(), getStatusText(response.getStatus()));
            log.info("Response Time: {}ms", responseTime);
            log.info("Content-Type: {}, Buffer Size: {}", response.getContentType(), response.getBufferSize());
            
        } catch (Exception e) {
            log.warn("Failed to log outgoing response: {}", e.getMessage());
        }
    }
    
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // Mask sensitive headers
            if ("authorization".equalsIgnoreCase(headerName)) {
                headers.put(headerName, headerValue != null ? "PRESENT (length: " + headerValue.length() + ")" : "NOT PRESENT");
            } else if ("cookie".equalsIgnoreCase(headerName)) {
                headers.put(headerName, headerValue != null ? "PRESENT (length: " + headerValue.length() + ")" : "NOT PRESENT");
            } else {
                headers.put(headerName, headerValue);
            }
        }
        
        return headers;
    }
    
    private Map<String, String> extractParameters(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        java.util.Enumeration<String> paramNames = request.getParameterNames();
        
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            
            if (paramValues.length == 1) {
                params.put(paramName, paramValues[0]);
            } else {
                params.put(paramName, String.join(", ", paramValues));
            }
        }
        
        return params;
    }
    
    private String getFullUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        
        if (serverPort != 80 && serverPort != 443) {
            url.append(":").append(serverPort);
        }
        
        url.append(requestURI);
        
        if (queryString != null) {
            url.append("?").append(queryString);
        }
        
        return url.toString();
    }
    
    private String getRequestBody(ContentCachingRequestWrapper wrapper) {
        try {
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.warn("Failed to read request body: {}", e.getMessage());
        }
        return null;
    }
    
    private String getResponseBody(ContentCachingResponseWrapper wrapper) {
        try {
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.warn("Failed to read response body: {}", e.getMessage());
        }
        return null;
    }
    
    private String getStatusText(int status) {
        switch (status) {
            case 200: return "OK";
            case 201: return "Created";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Unknown";
        }
    }
}
