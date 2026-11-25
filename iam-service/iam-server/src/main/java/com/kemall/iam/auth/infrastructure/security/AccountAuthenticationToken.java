package com.kemall.iam.auth.infrastructure.security;

import com.kemall.iam.api.enums.AccountType;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * 支持多账号类型（USERNAME / MOBILE / EMAIL）的认证令牌。
 * <p>
 * 继承 {@link AbstractAuthenticationToken}，
 * 在 credentials（密码）不变的基础上增加 {@code accountType}，
 * 让 {@link AccountAuthenticationProvider} 能根据类型选择查询策略。
 */
public class AccountAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1L;

    @Getter
    private final AccountType accountType;

    private final Object principal;

    private Object credentials;

    /**
     * 未认证构造（发起 authenticate() 调用前用）
     */
    public AccountAuthenticationToken(Object principal,
                                      Object credentials,
                                      AccountType accountType) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        this.accountType = accountType;
    }

    /**
     * 已认证构造（Provider 内部认证成功后用）
     */
    public AccountAuthenticationToken(Object principal,
                                      Object credentials,
                                      Collection<? extends GrantedAuthority> authorities,
                                      AccountType accountType) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.accountType = accountType;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
