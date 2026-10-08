package edu.unapec.rh.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Getter
@Setter
public class Empleado extends BaseEntity {
  @OneToOne(optional = false)
  @JoinColumn(name = "candidato_id", unique = true, nullable = false)
  private Candidato candidato;

  @Column(nullable = false, unique = true, length = 11)
  private String cedula;

  @Column(nullable = false, length = 210)
  private String nombre;

  @Column(nullable = false)
  private LocalDate fechaIngreso;

  @Column(nullable = false, length = 100)
  private String departamento;

  @ManyToOne(optional = false)
  @JoinColumn(name = "puesto_id", nullable = false)
  private Puesto puesto;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal salarioMensual;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Estado estado = Estado.ACTIVO;
}
