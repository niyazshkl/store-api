package com.codewithmosh.store.auth;

import com.codewithmosh.store.services.Jwt;
import com.codewithmosh.store.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    /**
     * Same contract as for {@code doFilter}, but guaranteed to be
     * just invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param request
     * @param response
     * @param filterChain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        /*
        There must be a jwt
        Check and ensure the client has given a valid jwt
         */
        String authHeader = request.getHeader("Authorization");

        if (authHeader==null || !authHeader.startsWith("Bearer ")) {
             filterChain.doFilter(request, response);
             /*
             I had a question: why do we need to call filterChain::doFilter to pass along?
             because otherwise, every request without a token is blocked, including login (open endpoint), so can never get a jwt token
             The reason this works is because one of the next filters in the chain is
             AuthorizationFilter (if you have spring security dependency in pom.xml) which checks the endpoint in the request
                - if the endpoint is authenticated and there is nothing set in the SecurityContextHolder, then it blocks at that stage (throws /403)
             */
             return; //this just means no need to do any extra work in this stage (i.e. no more checks and no setting SecurityContextHolder)
        }

        String token = authHeader.replace("Bearer ", "");
        Jwt jwt = null;
        try {
            jwt = jwtService.parse(token);
            boolean isValid = jwt.isUnexpired();
            if (!isValid) {
                filterChain.doFilter(request, response);
                 /*
                 I had a question: why do we need to call filterChain::doFilter to pass along?
                 because otherwise, every request without a token is blocked, including login (open endpoint), so can never get a jwt token
                 The reason this works is because one of the next filters in the chain is
                 AuthorizationFilter (if you have spring security dependency in pom.xml) which checks the endpoint in the request
                    - if the endpoint is authenticated and there is nothing set in the SecurityContextHolder, then it blocks at that stage (throws /403)
                 */
                return; //this just means no need to do any extra work in this stage (i.e. no more checks and no setting SecurityContextHolder)
            }
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            throw new RuntimeException(e);
            //this just means no need to do any extra work in this stage (i.e. no more checks and no setting SecurityContextHolder)
        }

        /*
        Valid jwt was sent,
        1. Tell spring who it is (pass subject of jwt to the Authentiction)
        2. Allow access to endpoints (setting third parameter of UserPasswordAuthenticationToken to be something)
        lets fetch the user so that security context knowns who it is.
        allowing access to protected endpoints
         */
        try {
            jwt = jwtService.parse(token);
            String role = jwt.getRole().name();
            String subject = (String) jwt.getSubject(); //Long userId cast
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    subject,
                    null, //no need for authenticated user!
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)) //no roles/permission for now
            );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); //this is boiler-plate code: basically it adds some meta addres like IP address to authentication object

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            throw new RuntimeException(e);
        }
    }

}
