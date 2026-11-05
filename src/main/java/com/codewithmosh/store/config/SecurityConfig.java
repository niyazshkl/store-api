package com.codewithmosh.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration //marks as a bean so IOC container can create an instance
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception { //SecurityFilterChain interface defines how HTTP requests are secure
        /*
        HttpSecurity is a builder object. Specify three things before building the SecurityFilterChain object
        1. Stateless session (token based, not session based authentication)
        2. Disable CSRF (an attack where the browser can send requests on the user's behalf without the user's knowledge)
        3. Specifying which endpoints are public/authorized
         */
        httpSecurity.sessionManagement(
                customizer->
                customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //step 1 is done
                .csrf(customizer->customizer.disable()) //step 2 is done
                .authorizeHttpRequests(customizer ->
                        customizer.requestMatchers("/carts/**").permitAll()
                                  .requestMatchers(HttpMethod.POST,"/users").permitAll()
                                  .anyRequest().authenticated()
                );
        return httpSecurity.build();
    }

    @Bean //anytime in the spring app when we need a bean, we get this one.
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(); //most secure & recommended password encoder
    }
}
