package com.hellobill.thing;

import com.hellobill.common.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// The logic lives here. Missing things throw NotFoundException (404), bad input BadRequestException (400).
@Service
public class ThingService {

  private final ThingRepository things;

  public ThingService(ThingRepository things) {
    this.things = things;
  }

  @Transactional(readOnly = true)
  public ThingResponse get(Long id) {
    return ThingResponse.from(find(id));
  }

  @Transactional(readOnly = true)
  public List<ThingResponse> list() {
    return things.findAllByOrderByNameAsc().stream().map(ThingResponse::from).toList();
  }

  @Transactional
  public ThingResponse create(ThingRequest request) {
    return ThingResponse.from(things.save(new Thing(request.name().trim())));
  }

  Thing find(Long id) {
    return things.findById(id).orElseThrow(() -> new NotFoundException("Thing " + id + " not found"));
  }
}
