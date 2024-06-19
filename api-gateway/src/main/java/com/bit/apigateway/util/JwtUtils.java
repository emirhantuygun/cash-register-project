package com.bit.apigateway.util;

import com.bit.apigateway.exception.InvalidTokenException;
import com.bit.apigateway.exception.TokenNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import javax.crypto.SecretKey;
import java.util.List;

@Component
@Log4j2
public class JwtUtils {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.authorities-key}")
    private String AUTHORITIES_KEY;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private String redisPort;
    private Jedis jedis;
    @PostConstruct
    public void init() {
        this.jedis = new Jedis(redisHost, Integer.parseInt(redisPort));
    }

    public Claims getClaimsAndValidate(String token) {
        try {
            return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();

        } catch (JwtException | IllegalArgumentException e) {

            throw new InvalidTokenException("Invalid token");
        }
    }

    public boolean isLoggedOut(String token) {

        String tokenIdStr = jedis.get(token);
        if (tokenIdStr == null) {
            throw new TokenNotFoundException("Token not found in Redis");
        }

        long tokenId = Long.parseLong(tokenIdStr);
        String key = "token:" + tokenId + ":is_logged_out";

        String value = jedis.get(key);
        if (value == null) {
            throw new TokenNotFoundException("Token's logout status information not found in Redis");
        }

        return Boolean.parseBoolean(value);
    }


    public List<String> getRoles(Claims claims) {
        return (List<String>) claims.get(AUTHORITIES_KEY);
    }

    protected SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(SIGNER_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}