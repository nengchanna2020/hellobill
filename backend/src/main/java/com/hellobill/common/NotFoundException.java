package com.hellobill.common;

public class NotFoundException extends RuntimeException {

  public NotFoundException(String what, Long id) {
    super(what + " " + id + " not found");
  }
}
