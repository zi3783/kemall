package com.kemall.iam.customer.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kemall.iam.customer.infrastructure.persistence.entity.CustomerPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消费者身份 Mapper。
 */
@Mapper
public interface CustomerMapper extends BaseMapper<CustomerPO> {
}
