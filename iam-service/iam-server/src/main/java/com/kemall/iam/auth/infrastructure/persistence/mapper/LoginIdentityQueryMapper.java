package com.kemall.iam.auth.infrastructure.persistence.mapper;

import com.kemall.iam.auth.infrastructure.persistence.entity.LoginIdentityRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 登录身份统计查询 Mapper。
 */
@Mapper
public interface LoginIdentityQueryMapper {

    LoginIdentityRow selectIdentityCounts(@Param("userId") Long userId);
}
