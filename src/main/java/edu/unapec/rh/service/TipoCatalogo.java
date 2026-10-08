package edu.unapec.rh.service;

import edu.unapec.rh.exception.NotFoundException;
import lombok.Getter;

@Getter
public enum TipoCatalogo {
  COMPETENCIAS("competencias", "Competencias", "Competencia", "Descripción"),
  IDIOMAS("idiomas", "Idiomas", "Idioma", "Nombre"),
  CAPACITACIONES("capacitaciones", "Capacitaciones", "Capacitación", "Descripción"),
  PUESTOS("puestos", "Puestos", "Puesto", "Nombre");
  private final String ruta, titulo, singular, campo;

  TipoCatalogo(String ruta, String titulo, String singular, String campo) {
    this.ruta = ruta;
    this.titulo = titulo;
    this.singular = singular;
    this.campo = campo;
  }

  public static TipoCatalogo ruta(String ruta) {
    for (var t : values()) if (t.ruta.equals(ruta)) return t;
    throw new NotFoundException();
  }
}
