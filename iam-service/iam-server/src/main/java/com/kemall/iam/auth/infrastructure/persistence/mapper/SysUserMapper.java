package com.kemall.iam.auth.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kemall.iam.auth.infrastructure.persistence.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {

    SysUserEntity selectByUsername(@Param("username") String username);

    SysUserEntity selectByIdentity(
            @Param("identityType") String identityType,
            @Param("identityValue") String identityValue
    );

    int updateLastLoginTime(
            @Param("userId") Long userId,
            @Param("loginTime") LocalDateTime loginTime
    );
}