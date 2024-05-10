package com.bit.authservice.config;

import com.bit.authservice.entity.Token;
import com.bit.authservice.repository.TokenRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import redis.clients.jedis.Jedis;

//  CACHE KULLANARAK LOGUT DURUMUNU CHECK EDEBİLİRSİN
//  ŞUAN İÇİN API GATEWAY AUTH SERVICE TOKEN TABLE'INI SORGULUYOR

@Configuration
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenRepository tokenRepository;

    private Jedis jedis;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private String redisPort;
    @PostConstruct
    public void init() {
        this.jedis = new Jedis(redisHost, Integer.parseInt(redisPort));
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authHeader.substring(7);
        Token storedToken = tokenRepository.findByToken(token).orElse(null);

        if(storedToken != null) {
            storedToken.setLoggedOut(true);
            tokenRepository.save(storedToken);
            jedis.set("token:" + storedToken.getId() + ":is_logged_out", "true");
        }
    }
}
