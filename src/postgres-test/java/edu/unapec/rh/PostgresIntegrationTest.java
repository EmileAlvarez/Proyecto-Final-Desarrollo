package edu.unapec.rh;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Ejecuta la misma suite sobre un PostgreSQL real, temporal y aislado, sin Docker. */
class PostgresIntegrationTest extends SistemaIntegrationTest {
  private static final EmbeddedPostgres POSTGRES = iniciar();

  private static EmbeddedPostgres iniciar() {
    try {
      return EmbeddedPostgres.builder().setServerConfig("listen_addresses", "127.0.0.1").start();
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @DynamicPropertySource
  static void postgres(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
    registry.add("spring.datasource.username", () -> "postgres");
    registry.add("spring.datasource.password", () -> "postgres");
  }

  @AfterAll
  static void cerrarPostgres() throws IOException {
    POSTGRES.close();
  }
}
