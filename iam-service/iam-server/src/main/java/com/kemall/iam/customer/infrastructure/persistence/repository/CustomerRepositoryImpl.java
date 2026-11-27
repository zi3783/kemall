package com.kemall.iam.customer.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kemall.iam.customer.domain.model.Customer;
import com.kemall.iam.customer.domain.repository.CustomerRepository;
import com.kemall.iam.customer.infrastructure.persistence.entity.CustomerPO;
import com.kemall.iam.customer.infrastructure.persistence.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 消费者身份仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerMapper customerMapper;

    @Override
    public Optional<Customer> findById(Long customerId) {
        return Optional.ofNullable(customerMapper.selectById(customerId)).map(this::toDomain);
    }

    @Override
    public Optional<Customer> findByUserId(Long userId) {
        CustomerPO po = customerMapper.selectOne(Wrappers.<CustomerPO>lambdaQuery()
                .eq(CustomerPO::getUserId, userId)
                .last("LIMIT 1"));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Long insert(Customer customer) {
        LocalDateTime now = LocalDateTime.now();
        CustomerPO po = new CustomerPO()
                .setId(customer.getId() == null ? IdWorker.getId() : customer.getId())
                .setUserId(customer.getUserId())
                .setStatus(customer.getStatus())
                .setCreatedAt(now)
                .setUpdatedAt(now);
        customerMapper.insert(po);
        customer.setId(po.getId());
        customer.setCreatedAt(now);
        return po.getId();
    }

    private Customer toDomain(CustomerPO po) {
        if (po == null) {
            return null;
        }
        return Customer.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .status(po.getStatus())
                .createdAt(po.getCreatedAt())
                .build();
    }
}
