package com.micro.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.micro.orderservice.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
