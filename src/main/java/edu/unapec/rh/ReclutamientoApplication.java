package edu.unapec.rh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    exclude =
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
            .class)
public class ReclutamientoApplication {
  public static void main(String[] args) {
    SpringApplication.run(ReclutamientoApplication.class, args);
  }
}
