package com.bit.apigateway.util;

import com.bit.apigateway.ApiGatewayApplication;
import com.bit.apigateway.exception.InvalidJwtTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.sql.*;
import java.util.List;

@Component
public class JwtUtils {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.authorities-key}")
    private String AUTHORITIES_KEY;

    @Value("${database.url}")
    private String URL;

    @Value("${database.username}")
    private String USERNAME;

    @Value("${database.password}")
    private String PASSWORD;

    private static final Logger logger = LogManager.getLogger(ApiGatewayApplication.class);

    public Claims getClaimsAndValidate(String token) {
        try {
            return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();

        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new InvalidJwtTokenException("Invalid JWT Token");
        }
    }

    public boolean isLoggedOut(String token) {
        String sql = "SELECT is_logged_out FROM tokens WHERE token=?";
        boolean isLoggedOut = false;

        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    isLoggedOut = rs.getBoolean("is_logged_out");
                } else {
                    // Token not found in the database
                    logger.warn("Token '{}' not found in the database", token);
                }
            } catch (SQLException e) {
                logger.error("An error occurred while executing the query: {}", e.getMessage());
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            logger.error("JDBC operations failed: {}", e.getMessage());
            throw new RuntimeException("JDBC operations failed!");
        }

        return isLoggedOut;
    }


    @SuppressWarnings("unchecked")
    public List<String> getRoles(Claims claims) {
        return (List<String>) claims.get(AUTHORITIES_KEY);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(SIGNER_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}