package com.kemall.iam.customer.domain.repository;

import com.kemall.iam.customer.domain.model.Customer;

import java.util.Optional;

/**
 * 消费者身份仓储端口。
 */
public interface CustomerRepository {

    Optional<Customer> findById(Long customerId);

    Optional<Customer> findByUserId(Long userId);

    Long insert(Customer customer);
}
