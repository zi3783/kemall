package com.kemall.iam.api.dto.customer;

import com.kemall.iam.api.dto.user.CurrentUserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 消费者个人资料：消费者身份 + 账号资料（含绑定身份）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;

    private CurrentUserInfo account;
}
