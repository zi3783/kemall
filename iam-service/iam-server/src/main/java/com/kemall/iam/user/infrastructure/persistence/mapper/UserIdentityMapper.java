package com.kemall.iam.user.infrastructure.persistence.mapper;

import com.kemall.iam.user.infrastructure.persistence.entity.UserIdentityPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * user_identity 持久化 Mapper（user 子域）。
 */
@Mapper
public interface UserIdentityMapper {

    List<UserIdentityPO> selectByUserId(@Param("userId") Long userId);

    boolean existsIdentity(
            @Param("identityType") String identityType,
            @Param("identityValue") String identityValue);

    int insert(UserIdentityPO po);

    void deleteByIdentityType(
            @Param("userId") Long userId,
            @Param("identityType") String identityType);

    int countVerifiedByUserId(@Param("userId") Long userId);
}
