package com.codewithmosh.store.config;

import com.codewithmosh.store.entities.Role;
import com.codewithmosh.store.filters.JwtFilter;
import lombok.AllArgsConstructor;
import org.springdoc.core.customizers.DataRestDelegatingMethodParameterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration //marks as a bean so IOC container can create an instance
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, DataRestDelegatingMethodParameterCustomizer dataRestDelegatingMethodParameterCustomizer) throws Exception { //SecurityFilterChain interface defines how HTTP requests are secure
        /*
        HttpSecurity is a builder object. Specify three things before building the SecurityFilterChain object
        1. Stateless session (token based, not session based authentication)
        2. Disable CSRF (an attack where the browser can send requests on the user's behalf without the user's knowledge)
        3. Specifying which endpoints are public/authorized
        Remember at this state, all authorization decisions made are directly from SecurityContextHolder (set in custom auth filter)
         */
        httpSecurity.sessionManagement(
                customizer->
                customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //step 1 is done
                .csrf(customizer->customizer.disable()) //step 2 is done
                .authorizeHttpRequests(customizer ->
                        customizer.requestMatchers("/carts/**").permitAll()
                                  .requestMatchers(HttpMethod.POST,"/users").permitAll()
                                  .requestMatchers("/admin/**").hasRole(Role.ADMIN.name())
                                  .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                                  .requestMatchers(HttpMethod.POST, "/auth/validate").permitAll()
                                  .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                                  .anyRequest().authenticated()
                )
                .exceptionHandling(
                        customizer->
                        {
                                customizer.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                                customizer.accessDeniedHandler((request, response, accessDeniedException) -> {
                                    response.setStatus(HttpStatus.FORBIDDEN.value());
                                });
                        }
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean //anytime in the spring app when we need a bean, we get this one.
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(); //most secure & recommended password encoder
    }

    @Bean
    public AuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

}
