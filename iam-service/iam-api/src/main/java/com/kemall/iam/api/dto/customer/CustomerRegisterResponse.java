package com.kemall.iam.api.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 消费者注册响应。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisterResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;

    private Long userId;

    private String username;
}
