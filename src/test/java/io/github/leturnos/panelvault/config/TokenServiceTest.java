package io.github.leturnos.panelvault.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.github.leturnos.panelvault.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private TokenService tokenService;
    private final String secret = "super-secret-test-key-for-jwt-generation-123456";

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", secret);
    }

    @Test
    @DisplayName("Should generate valid JWT token with subject and expiration")
    void generateToken_shouldGenerateValidToken_whenUserProvided() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        String token = tokenService.generateToken(user);

        assertThat(token).isNotBlank();

        String subject = tokenService.validateToken(token);
        assertThat(subject).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should return subject when validating a valid token")
    void validateToken_shouldReturnSubject_whenTokenIsValid() {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        String token = JWT.create()
                .withIssuer("panel-vault")
                .withSubject("anotheruser")
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(algorithm);

        String subject = tokenService.validateToken(token);

        assertThat(subject).isEqualTo("anotheruser");
    }

    @Test
    @DisplayName("Should return null when token has an invalid signature")
    void validateToken_shouldReturnNull_whenSignatureIsInvalid() {
        Algorithm differentAlgorithm = Algorithm.HMAC256("different-secret-key-that-does-not-match");
        String token = JWT.create()
                .withIssuer("panel-vault")
                .withSubject("testuser")
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(differentAlgorithm);

        String subject = tokenService.validateToken(token);

        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("Should return null when token is expired")
    void validateToken_shouldReturnNull_whenTokenIsExpired() {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        String expiredToken = JWT.create()
                .withIssuer("panel-vault")
                .withSubject("expireduser")
                .withExpiresAt(Instant.now().minusSeconds(3600))
                .sign(algorithm);

        String subject = tokenService.validateToken(expiredToken);

        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("Should return null when issuer does not match")
    void validateToken_shouldReturnNull_whenIssuerIsInvalid() {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        String token = JWT.create()
                .withIssuer("wrong-issuer")
                .withSubject("testuser")
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(algorithm);

        String subject = tokenService.validateToken(token);

        assertThat(subject).isNull();
    }
}
