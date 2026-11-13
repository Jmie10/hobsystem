package com.hob.hobsystem.repository;

import com.hob.hobsystem.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {
    List<WaitlistEntry> findAllByOrderByIdAsc();
}
