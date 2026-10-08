package edu.unapec.rh.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.*;

@Entity
@Getter
@Setter
public class Candidato extends BaseEntity {
  @Column(nullable = false, unique = true, length = 11)
  private String cedula;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false, length = 100)
  private String apellidos;

  @Column(nullable = false, length = 180)
  private String email;

  @Column(length = 30)
  private String telefono;

  @ManyToOne(optional = false)
  @JoinColumn(name = "puesto_id", nullable = false)
  private Puesto puesto;

  @Column(nullable = false, length = 100)
  private String departamento;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal salarioAspirado;

  @Column(length = 180)
  private String recomendadoPor;

  @Column(nullable = false, updatable = false)
  private LocalDateTime fechaRegistro = LocalDateTime.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private EstadoCandidato estado = EstadoCandidato.REGISTRADO;

  @ManyToMany
  @JoinTable(
      name = "candidato_competencia",
      joinColumns = @JoinColumn(name = "candidato_id"),
      inverseJoinColumns = @JoinColumn(name = "competencia_id"))
  private Set<Competencia> competencias = new LinkedHashSet<>();

  @ManyToMany
  @JoinTable(
      name = "candidato_capacitacion",
      joinColumns = @JoinColumn(name = "candidato_id"),
      inverseJoinColumns = @JoinColumn(name = "capacitacion_id"))
  private Set<Capacitacion> capacitaciones = new LinkedHashSet<>();

  @OneToMany(mappedBy = "candidato", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id")
  private List<CandidatoIdioma> idiomas = new ArrayList<>();

  @OneToMany(mappedBy = "candidato", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("fechaDesde DESC, id DESC")
  private List<ExperienciaLaboral> experiencias = new ArrayList<>();

  public String getNombreCompleto() {
    return nombre + " " + apellidos;
  }

  public boolean isElegible() {
    return estado != EstadoCandidato.CONTRATADO && estado != EstadoCandidato.RECHAZADO;
  }
}
