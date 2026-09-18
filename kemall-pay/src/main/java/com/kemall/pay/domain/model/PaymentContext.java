package com.kemall.pay.domain.model;

import com.kemall.pay.domain.enums.PaymentStatusEnum;
import com.kemall.pay.domain.state.PaymentStatus;
import com.kemall.pay.domain.state.impl.ClosedStatus;
import com.kemall.pay.domain.state.impl.FailedStatus;
import com.kemall.pay.domain.state.impl.PendingStatus;
import com.kemall.pay.domain.state.impl.SuccessStatus;
import lombok.Getter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import static com.kemall.pay.domain.enums.PaymentStatusEnum.*;

@Getter
public class PaymentContext {

   private PaymentStatusEnum statusEnum;

    public static final Map<PaymentStatusEnum, PaymentStatus> STATE_MAP;

    static {
        EnumMap<PaymentStatusEnum, PaymentStatus> map = new EnumMap<>(PaymentStatusEnum.class);
        map.put(SUCCESS, new SuccessStatus());
        map.put(FAILED, new FailedStatus());
        map.put(CLOSED, new ClosedStatus());
        map.put(PENDING, new PendingStatus());
        //todo 退款没写
        STATE_MAP = Collections.unmodifiableMap(map);
    }

   public void changeStatus(PaymentStatusEnum statusEnum) {
       this.statusEnum = statusEnum;
   }

   public void setStatusEnum(PaymentStatusEnum statusEnum) {
        this.statusEnum = statusEnum;
   }

   public void paySuccess() {
        STATE_MAP.get(statusEnum).paySuccess(this);
   }

   public void payFailed() {
        STATE_MAP.get(statusEnum).payFail(this);
   }

   public void closed() {
        STATE_MAP.get(statusEnum).close(this);
   }

}
