package in.aman.tasks.taskSecurityConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
import java.util.List;

@Component
public class JwtTokenValidator extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwtHeader = request.getHeader(JwtConstant.JWT_HEADER);

        System.out.println("JWT HEADER RECEIVED = " + jwtHeader);
        System.out.println("JWT SECRET IN VALIDATOR = " + secret);

        if (jwtHeader != null && jwtHeader.startsWith("Bearer ")) {

            String jwt = jwtHeader.substring(7);

            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(secret)
                        .parseClaimsJws(jwt)
                        .getBody();

                String email = claims.get("email", String.class);
                String authorities = claims.get("authorities", String.class);

                List<GrantedAuthority> auth =
                        AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(email, null, auth);

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                throw new BadCredentialsException("Invalid JWT token", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
