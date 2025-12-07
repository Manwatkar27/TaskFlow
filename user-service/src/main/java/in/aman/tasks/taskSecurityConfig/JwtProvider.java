package in.aman.tasks.taskSecurityConfig;

import in.aman.tasks.usermodel.User;
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

    public String generateToken(Authentication authentication) {

        String email = authentication.getName();
        String role = "ROLE_USER";          //  default safety

        //  FORCE ROLE FROM DB ENTITY — THIS IS THE FIX
        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            role = user.getRole();          // ✅ returns ROLE_ADMIN for admin logins
        }

        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String jwt = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .claim("email", email)
                .claim("role", role)
                .claim("authorities", role)   // ✅ REQUIRED by your validator
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        //  CONFIRMATION LOGS
        System.out.println("JWT EMAIL = " + email);
        System.out.println("JWT ROLE USED = " + role);
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
