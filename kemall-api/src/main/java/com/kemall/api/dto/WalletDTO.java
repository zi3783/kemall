package com.kemall.api.dto;

import com.kemall.api.enums.TransactionType;
import lombok.Data;

@Data
public class WalletDTO {
    Long userId;
    Long balance;
    TransactionType transactionType;
}
