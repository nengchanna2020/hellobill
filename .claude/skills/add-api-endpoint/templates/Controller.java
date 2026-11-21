package com.hellobill.thing;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Thin: map the HTTP call to one service method. No logic, no repositories.
@RestController
@RequestMapping("/api/things")
public class ThingController {

  private final ThingService service;

  public ThingController(ThingService service) {
    this.service = service;
  }

  @GetMapping("/{id}")
  public ThingResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @GetMapping
  public List<ThingResponse> list() {
    return service.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ThingResponse create(@Valid @RequestBody ThingRequest request) {
    return service.create(request);
  }
}
