package edu.unapec.rh.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Getter
@Setter
public class Puesto extends CatalogoBase {
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Riesgo riesgo;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal salarioMinimo;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal salarioMaximo;
}
