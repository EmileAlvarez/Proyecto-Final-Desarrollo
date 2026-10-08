package edu.unapec.rh.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
  private final String field;

  public BusinessException(String message) {
    this(null, message);
  }

  public BusinessException(String field, String message) {
    super(message);
    this.field = field;
  }
}
