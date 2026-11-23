package com.seaandtea.controller;

import com.seaandtea.dto.AuthResponse;
import com.seaandtea.dto.LoginRequest;
import com.seaandtea.dto.RegisterRequest;
import com.seaandtea.oauth.OAuthProperties;
import com.seaandtea.oauth.OAuthRedirects;
import com.seaandtea.oauth.OAuthReturnCookie;
import com.seaandtea.oauth.OAuthReturnTargets;
import com.seaandtea.security.AllowedOrigins;
import com.seaandtea.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    
    private final AuthService authService;
    private final OAuthProperties oauthProperties;
    private final OAuthReturnTargets oauthReturnTargets;
    private final OAuthRedirects oauthRedirects;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/providers")
    @Operation(summary = "Social login providers", description = "Reports which social sign-in providers have credentials configured")
    public Map<String, Boolean> providers() {
        return Map.of(
                "google", oauthProperties.configured("google"),
                "facebook", oauthProperties.configured("facebook")
        );
    }

    @GetMapping("/oauth/{provider}")
    @Operation(summary = "Start social sign-in", description = "Redirects the browser to Google or Facebook")
    public void startOAuth(
            @PathVariable String provider,
            @RequestParam(value = "returnTo", required = false) String returnTo,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        if (!"google".equals(provider) && !"facebook".equals(provider)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String origin = AllowedOrigins.normalizeOrigin(returnTo);
        if (origin == null) {
            origin = oauthReturnTargets.configuredOrigin();
        }
        request.setAttribute("oauth.return", origin);
        if (!oauthProperties.configured(provider)) {
            oauthRedirects.toApp(request, response, null, "This sign-in option is not configured yet.");
            return;
        }
        OAuthReturnCookie.write(request, response, origin);
        response.sendRedirect(request.getContextPath() + "/oauth2/authorization/" + provider);
    }
}

