package edu.unapec.rh.config;

import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

@Configuration
public class WebConfig {

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/vendor/**",
                        "/images/**",
                        "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login?error")
                    .permitAll())
        .logout(
            logout ->
                logout.logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll())
        .build();
  }

  @Bean
  UserDetailsService users() {
    UserDetails admin =
        User.withUsername("admin123")
            .password("{noop}admin123")
            .roles("ADMIN")
            .build();
    return new InMemoryUserDetailsManager(admin);
  }

  @Bean
  LocaleResolver localeResolver() {
    return new FixedLocaleResolver(Locale.forLanguageTag("es-DO"));
  }
}
