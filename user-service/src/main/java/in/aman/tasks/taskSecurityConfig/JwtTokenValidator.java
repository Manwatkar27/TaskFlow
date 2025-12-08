package in.aman.tasks.taskSecurityConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Component
public class JwtTokenValidator extends OncePerRequestFilter {

    @Value("${jwt.secret:}")   // ✅ Default empty string prevents NPE
    private String secret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String jwtHeader = request.getHeader("Authorization");

        System.out.println("JWT HEADER RECEIVED = " + jwtHeader);
        System.out.println("JWT SECRET IN VALIDATOR = " + secret);

        // ✅ If no Authorization header → skip validation
        if (jwtHeader == null || !jwtHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ If JWT secret is missing → explicit error (prevents NPE)
        if (secret == null || secret.isBlank()) {
            throw new RuntimeException("JWT_SECRET is NOT set in environment variables!");
        }

        String jwt = jwtHeader.substring(7);

        try {
            // ✅ Build key safely
            Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            String email = claims.get("email", String.class);
            String authorities = claims.get("authorities", String.class);

            if (authorities == null) authorities = "";

            List<GrantedAuthority> authList =
                    AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authList);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            System.out.println("JWT VALIDATION FAILED = " + e.getMessage());
            throw new BadCredentialsException("Invalid JWT token", e);
        }

        filterChain.doFilter(request, response);
    }
}
