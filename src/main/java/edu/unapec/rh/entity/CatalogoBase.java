package edu.unapec.rh.entity;

import jakarta.persistence.*;
import java.text.Normalizer;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class CatalogoBase extends BaseEntity {
  @Column(nullable = false, length = 180)
  private String nombre;

  @Column(nullable = false, unique = true, length = 180)
  private String clave;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Estado estado = Estado.ACTIVO;

  public static String normalizar(String value) {
    return Normalizer.normalize(value.strip().replaceAll("\\s+", " "), Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT);
  }

  @PrePersist
  @PreUpdate
  public void normalizarNombre() {
    nombre = nombre.strip().replaceAll("\\s+", " ");
    clave = normalizar(nombre);
  }
}
