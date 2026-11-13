package com.hob.hobsystem.repository;

import com.hob.hobsystem.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findAllByOrderByIdAsc();
}
