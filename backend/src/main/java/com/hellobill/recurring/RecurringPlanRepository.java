package com.hellobill.recurring;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringPlanRepository extends JpaRepository<RecurringPlan, Long> {

  List<RecurringPlan> findAllByOrderByIdAsc();

  List<RecurringPlan> findByActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
}
