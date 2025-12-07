package in.aman.tasks.taskSecurityConfig;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(Authentication auth) {

        //ALWAYS correct email
        String email = auth.getName();

        // HARDCODE ROLE FETCH FROM PRINCIPAL STRING
        // Because your authorities are NOT being populated correctly
        String role = "ROLE_USER";

        if (auth.getPrincipal() != null) {
            String principal = auth.getPrincipal().toString();

            if (principal.contains("ROLE_ADMIN")) {
                role = "ROLE_ADMIN";
            }
        }

        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String jwt = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .claim("email", email)
                .claim("role", role)                // send role for frontend
                .claim("authorities", role)         // send authorities for backend
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // DEBUG LOGS

        System.out.println("JWT EMAIL = " + email);
        System.out.println("JWT ROLE USED = " + role);
        System.out.println("JWT AUTHORITIES USED = " + role);
        System.out.println("JWT SECRET USED IN PROVIDER = " + secret);
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
