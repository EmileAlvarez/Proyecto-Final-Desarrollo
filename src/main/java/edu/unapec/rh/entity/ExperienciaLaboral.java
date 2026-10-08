package edu.unapec.rh.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Getter
@Setter
public class ExperienciaLaboral extends BaseEntity {
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "candidato_id", nullable = false)
  private Candidato candidato;

  @Column(nullable = false, length = 180)
  private String empresa;

  @Column(nullable = false, length = 180)
  private String puestoOcupado;

  @Column(nullable = false)
  private LocalDate fechaDesde;

  private LocalDate fechaHasta;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal salario;
}
