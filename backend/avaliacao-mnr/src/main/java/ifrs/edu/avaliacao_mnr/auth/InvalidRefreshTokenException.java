package ifrs.edu.avaliacao_mnr.auth;

import org.springframework.security.core.AuthenticationException;

/** Thrown when a refresh token is unknown, expired, revoked, or belongs to an inactive user. */
public class InvalidRefreshTokenException extends AuthenticationException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
