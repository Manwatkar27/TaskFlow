package in.aman.tasks.taskSecurityConfig;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(Authentication auth) {

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        String roles = populateAuthorities(authorities);

        String jwt = Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .claim("email", auth.getName())
                .claim("authorities", roles)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();

        System.out.println("JWT SECRET USED IN PROVIDER = " + secret);
        System.out.println("Generated Token = " + jwt);

        return jwt;
    }

    private static String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<String> auths = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            auths.add(authority.getAuthority());
        }
        return String.join(",", auths);
    }

    public String getEmailFromJwtToken(String jwt) {

        jwt = jwt.replace("Bearer ", "");

        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(jwt)
                .getBody()
                .get("email", String.class);
    }
}
