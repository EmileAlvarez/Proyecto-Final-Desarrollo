package edu.unapec.rh.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Getter
@Setter
public class Capacitacion extends CatalogoBase {
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Nivel nivel;

  @Column(nullable = false)
  private LocalDate fechaDesde;

  private LocalDate fechaHasta;

  @Column(nullable = false, length = 180)
  private String institucion;
}
