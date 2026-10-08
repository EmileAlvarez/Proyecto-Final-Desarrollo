package edu.unapec.rh.dto;

import edu.unapec.rh.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import lombok.*;

@Getter
@Setter
public class CandidatoForm {

  private Long version;

  @NotBlank(message = "La cédula es obligatoria.")
  @Pattern(
      regexp = "[0-9]{11}",
      message = "La cédula debe contener exactamente 11 dígitos, sin letras ni guiones.")
  private String cedula;

  @NotBlank
  @Size(max = 100)
  private String nombre;

  @NotBlank
  @Size(max = 100)
  private String apellidos;

  @NotBlank
  @Email(message = "Escribe un correo válido.")
  @Pattern(
      regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
      message = "El correo debe tener un formato válido, por ejemplo nombre@correo.com.")
  @Size(max = 180)
  private String email;

  @Size(max = 15)
  @Pattern(
      regexp = "(?:|[0-9]{7,15})",
      message = "El teléfono debe contener únicamente números (entre 7 y 15 dígitos).")
  private String telefono;

  @NotNull(message = "Selecciona un puesto.")
  private Long puestoId;

  @NotBlank
  @Size(max = 100)
  private String departamento;

  @NotNull
  @DecimalMin("0.00")
  @Digits(integer = 12, fraction = 2)
  private BigDecimal salarioAspirado;

  @Size(max = 180)
  private String recomendadoPor;

  @Size(max = 100)
  private List<Long> competenciaIds = new ArrayList<>();

  @Size(max = 100)
  private List<Long> capacitacionIds = new ArrayList<>();

  @Valid
  @Size(max = 100)
  private List<IdiomaSeleccion> idiomas = new ArrayList<>();

  @Valid
  @Size(max = 30)
  private List<ExperienciaForm> experiencias = new ArrayList<>();
}
