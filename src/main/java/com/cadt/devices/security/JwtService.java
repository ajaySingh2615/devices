package com.cadt.devices.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long accessExpMinutes;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.issuer}") String issuer,
                      @Value("${security.jwt.access-exp-minutes}") long exp) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.issuer = issuer;
        this.accessExpMinutes = exp;
    }

    public String generate(String sub, Map<String, Object> claims) {
        var now = Instant.now();
        System.out.println("JwtService.generate() - subject: " + sub + ", claims: " + claims);
        
        String token = Jwts.builder()
                .setSubject(sub)
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessExpMinutes * 60)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
                
        System.out.println("Generated JWT token: " + token.substring(0, Math.min(token.length(), 50)) + "...");
        return token;
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}
