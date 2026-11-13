package com.hob.hobsystem.repository;

import com.hob.hobsystem.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByContactNumber(String contactNumber);
    Customer findByCustomerNo(String customerNo);
    Customer findByEmail(String email);
    List<Customer> findByUserRoleOrderByIdAsc(String userRole);
}
