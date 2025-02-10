package com.bank.api.repository;

import com.bank.api.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>
{
    Optional<Customer> findByName(String name);
}
