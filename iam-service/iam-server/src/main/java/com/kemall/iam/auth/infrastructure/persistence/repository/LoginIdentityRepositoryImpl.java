package com.kemall.iam.auth.infrastructure.persistence.repository;

import com.kemall.iam.auth.domain.model.LoginIdentity;
import com.kemall.iam.auth.domain.repository.LoginIdentityRepository;
import com.kemall.iam.auth.infrastructure.persistence.entity.LoginIdentityRow;
import com.kemall.iam.auth.infrastructure.persistence.mapper.LoginIdentityQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 登录身份解析仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class LoginIdentityRepositoryImpl implements LoginIdentityRepository {

    private final LoginIdentityQueryMapper loginIdentityQueryMapper;

    @Override
    public LoginIdentity resolve(Long userId) {
        LoginIdentityRow row = loginIdentityQueryMapper.selectIdentityCounts(userId);
        if (row == null) {
            return new LoginIdentity(false, false, 0);
        }
        return new LoginIdentity(
                row.getCustomerCount() != null && row.getCustomerCount() > 0,
                row.getPlatformCount() != null && row.getPlatformCount() > 0,
                row.getTenantCount() == null ? 0 : row.getTenantCount());
    }
}
