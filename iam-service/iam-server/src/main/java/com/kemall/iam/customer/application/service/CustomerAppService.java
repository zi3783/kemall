package com.kemall.iam.customer.application.service;

import com.kemall.iam.api.dto.customer.CustomerProfileInfo;
import com.kemall.iam.api.dto.customer.CustomerRegisterResponse;
import com.kemall.iam.api.dto.user.IdentityInfo;
import com.kemall.iam.api.dto.user.RegisterRequest;
import com.kemall.iam.api.dto.user.RegisterResponse;
import com.kemall.iam.api.dto.user.UpdateProfileRequest;
import com.kemall.iam.customer.domain.exception.CustomerBusinessException;
import com.kemall.iam.customer.domain.exception.CustomerErrorCode;
import com.kemall.iam.customer.domain.exception.CustomerNotFoundException;
import com.kemall.iam.customer.domain.model.Customer;
import com.kemall.iam.customer.domain.repository.CustomerRepository;
import com.kemall.iam.user.application.service.AccountAppService;
import com.kemall.iam.user.application.service.UserProfileAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 消费者应用服务：注册、个人资料、绑定身份查询。
 * <p>
 * 复用 user 子域的账号注册与资料能力，本子域只维护消费者身份（customer）。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerAppService {

    private final CustomerRepository customerRepository;
    private final AccountAppService accountAppService;
    private final UserProfileAppService userProfileAppService;

    /**
     * 消费者注册：先走通用账号注册（用户名/密码/手机号/验证码），
     * 再同事务写入 customer 身份。
     */
    @Transactional(rollbackFor = Exception.class)
    public CustomerRegisterResponse register(RegisterRequest request) {
        RegisterResponse account = accountAppService.register(request);

        Customer customer = Customer.builder()
                .userId(account.getUserId())
                .status(1)
                .build();
        try {
            customerRepository.insert(customer);
        } catch (DuplicateKeyException e) {
            throw new CustomerBusinessException(CustomerErrorCode.CUSTOMER_ALREADY_EXISTS,
                    Map.of("userId", account.getUserId()));
        }

        log.info("[CUSTOMER] 消费者注册成功 customerId={} userId={}", customer.getId(), account.getUserId());

        return CustomerRegisterResponse.builder()
                .customerId(customer.getId())
                .userId(account.getUserId())
                .username(account.getUsername())
                .build();
    }

    /**
     * 消费者个人资料：消费者身份 + 账号资料（含脱敏绑定身份）。
     */
    public CustomerProfileInfo getProfile(Long userId) {
        Customer customer = requireActiveCustomer(userId);
        return CustomerProfileInfo.builder()
                .customerId(customer.getId())
                .account(userProfileAppService.getCurrentUser(userId))
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        requireActiveCustomer(userId);
        userProfileAppService.updateProfile(userId, request);
    }

    /**
     * 消费者已绑定身份（手机号/邮箱）。
     */
    public List<IdentityInfo> listIdentities(Long userId) {
        requireActiveCustomer(userId);
        return userProfileAppService.getCurrentUser(userId).getIdentities();
    }

    private Customer requireActiveCustomer(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomerNotFoundException(userId));
        if (!customer.isActive()) {
            throw new CustomerBusinessException(CustomerErrorCode.CUSTOMER_UNAVAILABLE,
                    Map.of("userId", userId));
        }
        return customer;
    }
}
