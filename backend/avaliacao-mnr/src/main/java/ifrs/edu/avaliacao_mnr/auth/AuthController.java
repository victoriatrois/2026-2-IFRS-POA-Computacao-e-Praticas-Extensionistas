package ifrs.edu.avaliacao_mnr.auth;

import ifrs.edu.avaliacao_mnr.auth.dto.AuthTokenResponse;
import ifrs.edu.avaliacao_mnr.auth.dto.LoginRequest;
import ifrs.edu.avaliacao_mnr.auth.dto.RefreshRequest;
import ifrs.edu.avaliacao_mnr.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Login, access-token refresh, logout, and current-user profile")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
        @Operation(summary = "Log in", description = "Validates credentials and returns a JWT access token and a rotating refresh token.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credentials accepted"),
            @ApiResponse(responseCode = "401", description = "Email or password is invalid"),
            @ApiResponse(responseCode = "400", description = "Request validation failed")
        })
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request,
                                                   HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/refresh")
        @Operation(summary = "Refresh tokens", description = "Rotates a refresh token and returns a new access token and refresh token.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens rotated"),
            @ApiResponse(responseCode = "401", description = "Refresh token is unknown, expired, revoked, or belongs to an inactive user"),
            @ApiResponse(responseCode = "400", description = "Request validation failed")
        })
    public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody RefreshRequest request,
                                                     HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken(), httpRequest));
    }

    @PostMapping("/logout")
        @Operation(summary = "Log out", description = "Revokes the supplied refresh token. Repeating logout is safe.")
        @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Refresh token revoked or already invalid"),
            @ApiResponse(responseCode = "400", description = "Request validation failed")
        })
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request,
                                       HttpServletRequest httpRequest) {
        authService.logout(request.refreshToken(), httpRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
        @Operation(summary = "Get current user", description = "Returns the profile associated with the access token.")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user profile"),
            @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired")
        })
    public ResponseEntity<AuthTokenResponse.UserProfile> me(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(authService.profile(authenticatedUser));
    }
}
