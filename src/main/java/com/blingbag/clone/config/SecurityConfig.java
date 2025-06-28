package com.blingbag.clone.config;

import com.blingbag.clone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                // Public pages
                .antMatchers(
                    "/",
                    "/shop/**",
                    "/product/**",
                    "/category/**",
                    "/search/**",
                    "/register",
                    "/about",
                    "/contact",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**"
                ).permitAll()
                // Secured pages
                .antMatchers("/cart/**", "/checkout/**").authenticated()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .permitAll()
            .and()
            .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            .and()
            .rememberMe()
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400) // 1 day
            .and()
            .csrf()
                .ignoringAntMatchers("/h2-console/**") // Only for development
            .and()
            .headers()
                .frameOptions()
                .sameOrigin(); // For H2 console
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username -> {
            var user = userService.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
            
            return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRoles().toArray(new String[0]))
                .accountExpired(!user.isActive())
                .accountLocked(!user.isActive())
                .credentialsExpired(!user.isActive())
                .disabled(!user.isActive())
                .build();
        }).passwordEncoder(passwordEncoder);
    }

    // For development only - creates an admin user on startup
    @Bean
    public void initialize() {
        if (!userService.findByEmail("admin@blingbag.com").isPresent()) {
            var admin = new com.blingbag.clone.model.User();
            admin.setEmail("admin@blingbag.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setPhoneNumber("1234567890");
            admin.addRole("ROLE_ADMIN");
            admin.setActive(true);
            admin.setEmailVerified(true);
            userService.createUser(admin);
        }
    }
}
