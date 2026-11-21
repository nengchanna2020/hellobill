package com.hellobill.common;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns our exceptions into simple JSON errors (RFC 9457 problem details). */
@RestControllerAdvice
public class ApiErrorHandler {

  @ExceptionHandler(NotFoundException.class)
  ProblemDetail notFound(NotFoundException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(BadRequestException.class)
  ProblemDetail badRequest(BadRequestException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail invalid(MethodArgumentNotValidException e) {
    Map<String, String> fields =
        e.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    f -> f.getField(), f -> String.valueOf(f.getDefaultMessage()), (a, b) -> a));
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Some fields are not valid");
    problem.setProperty("fields", fields);
    return problem;
  }
}
