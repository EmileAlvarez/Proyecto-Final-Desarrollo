package edu.unapec.rh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"candidato_id", "idioma_id"}))
public class CandidatoIdioma extends BaseEntity {
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "candidato_id", nullable = false)
  private Candidato candidato;

  @ManyToOne(optional = false)
  @JoinColumn(name = "idioma_id", nullable = false)
  private Idioma idioma;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NivelIdioma nivel;
}
