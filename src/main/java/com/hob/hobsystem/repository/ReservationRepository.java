package com.hob.hobsystem.repository;

import com.hob.hobsystem.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByOrderByIdAsc();
}
