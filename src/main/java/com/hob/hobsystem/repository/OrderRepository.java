package com.hob.hobsystem.repository;

import com.hob.hobsystem.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findAllByOrderByIdAsc();
    List<OrderEntity> findByStatusOrderByIdAsc(String status);
}
