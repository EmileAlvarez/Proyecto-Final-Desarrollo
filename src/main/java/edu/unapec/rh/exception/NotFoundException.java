package edu.unapec.rh.exception;

public class NotFoundException extends RuntimeException {
  public NotFoundException() {
    super("El registro solicitado no existe.");
  }
}
