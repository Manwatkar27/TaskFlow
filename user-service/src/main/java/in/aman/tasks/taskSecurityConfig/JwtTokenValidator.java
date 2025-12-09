package in.aman.tasks.taskSecurityConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwtHeader = request.getHeader("Authorization");

        // ✅ Get secret DIRECTLY from OS ENV (Render safe)
        String secret = System.getenv("JWT_SECRET");

        // Fallback for local dev
        if (secret == null || secret.isBlank()) {
            secret = "local_jwt_secret_123";
        }

        System.out.println("JWT HEADER RECEIVED = " + jwtHeader);
        System.out.println("JWT SECRET USED = " + secret);

        if (jwtHeader == null || !jwtHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String jwt = jwtHeader.substring(7);

            Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            String email = claims.get("email", String.class);
            String authorities = claims.get("authorities", String.class);

            List<GrantedAuthority> authList =
                    AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authList);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            throw new BadCredentialsException("Invalid JWT token", e);
        }

        filterChain.doFilter(request, response);
    }
}
