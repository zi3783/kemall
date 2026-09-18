package com.kemall.api.dto;

import com.kemall.api.enums.TransactionType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;


@Getter
@Builder
public class WalletDTO implements Serializable {
    private final Long userId;
    private final Long balance;
    private final TransactionType transactionType;
    private final String paymentNo;
}
