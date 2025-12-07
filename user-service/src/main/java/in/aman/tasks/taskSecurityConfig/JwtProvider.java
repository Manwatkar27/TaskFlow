package in.aman.tasks.taskSecurityConfig;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(Authentication auth) {

        String email = auth.getName();

        // collect authorities as comma-separated string (e.g. "ROLE_ADMIN")
        String authorities = "";
        if (auth.getPrincipal() instanceof UserDetails userDetails) {
            Collection<? extends GrantedAuthority> auths = userDetails.getAuthorities();
            authorities = auths.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
        }

        // primary role – first one or fallback to ROLE_USER
        String role = (authorities == null || authorities.isEmpty())
                ? "ROLE_USER"
                : authorities.split(",")[0];

        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String jwt = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
                .claim("email", email)
                .claim("role", role)           // for frontend decoding
                .claim("authorities", authorities) // for backend validator
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("JWT ROLE USED = " + role);
        System.out.println("JWT AUTHORITIES USED = " + authorities);
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
