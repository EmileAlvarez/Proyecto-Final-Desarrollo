package edu.unapec.rh.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component("fmt")
public class Formato {
  public String dinero(BigDecimal value) {
    NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
    format.setMinimumFractionDigits(2);
    format.setMaximumFractionDigits(2);
    return "RD$ " + format.format(value == null ? BigDecimal.ZERO : value);
  }

  public String etiqueta(Object value) {
    if (value == null) return "—";
    String label =
        switch (value.toString()) {
          case "EN_EVALUACION" -> "En evaluación";
          case "POST_GRADO" -> "Posgrado";
          case "MAESTRIA" -> "Maestría";
          case "TECNICO" -> "Técnico";
          case "GESTION" -> "Gestión";
          case "BASICO" -> "Básico";
          default -> null;
        };
    if (label != null) return label;
    String text = value.toString().toLowerCase(Locale.ROOT).replace('_', ' ');
    return Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }

  public String cedula(String value) {
    return value.substring(0, 3) + "-" + value.substring(3, 10) + "-" + value.substring(10);
  }

  public String iniciales(String value) {
    String[] words = value.strip().split("\\s+");
    return (words[0].substring(0, 1)
            + (words.length > 1 ? words[words.length - 1].substring(0, 1) : ""))
        .toUpperCase(Locale.ROOT);
  }
}
