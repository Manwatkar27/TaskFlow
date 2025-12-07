package in.aman.tasks.taskSecurityConfig;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(Authentication auth) {

        String email = auth.getName();

        // ✅ ALWAYS WORKING ROLE FETCH
        String role = auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        if (role.isBlank()) {
            role = "ROLE_USER";
        }

        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String jwt = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .claim("email", email)
                .claim("role", role)
                .claim("authorities", role)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // ✅ FINAL DEBUG LOGS
        System.out.println("JWT EMAIL = " + email);
        System.out.println("JWT AUTHORITIES USED = " + role);
        System.out.println("JWT SECRET USED = " + secret);
        System.out.println("Generated Token = " + jwt);

        return jwt;
    }

    public String getEmailFromJwtToken(String jwt) {

        jwt = jwt.replace("Bearer ", "");

        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
    }
}
