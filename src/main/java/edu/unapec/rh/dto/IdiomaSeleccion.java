package edu.unapec.rh.dto;

import edu.unapec.rh.entity.*;
import jakarta.validation.constraints.*;
import java.util.*;
import lombok.*;

@Getter
@Setter
public class IdiomaSeleccion {

  @NotNull private Long idiomaId;
  private boolean seleccionado;
  @NotNull private NivelIdioma nivel = NivelIdioma.INTERMEDIO;
}
