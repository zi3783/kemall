package com.kemall.iam.auth.infrastructure.security;

import com.kemall.iam.auth.application.port.PasswordEncoder;
import com.kemall.iam.auth.domain.model.User;
import com.kemall.iam.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * IAM 账号密码认证 Provider（Spring Security AuthenticationProvider）。
 * <p>
 * 职责（Spring Security 认证体系内）：
 * <ol>
 *   <li>根据 {@code accountType} 选择查询策略（用户名/手机号/邮箱）</li>
 *   <li>校验账号是否存在 + 密码哈希匹配（委派给领域 {@link PasswordEncoder} 端口）</li>
 *   <li>校验账户登录状态（{@link User#canLogin()}）</li>
 *   <li>成功 → 返回 principal 为领域 {@link User} 的 {@link AccountAuthenticationToken}（已认证状态）</li>
 *   <li>失败 → 抛 Spring Security 标准异常（由上层 ACL 映射为领域异常）</li>
 * </ol>
 * <p>
 * 严格遵循 DDD 依赖方向：
 * 依赖 {@link UserRepository}（Domain Port）和 {@link PasswordEncoder}（Application Port），
 * 不依赖任何 SQL / Redis / MyBatis 细节。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (!(authentication instanceof AccountAuthenticationToken token)) {
            return null; // 不支持，交给下一个 Provider
        }

        String account = token.getName();
        String rawPassword = token.getCredentials() == null
                ? null : token.getCredentials().toString();

        // 1. 查用户（多账号类型）
        User user = userRepository.findByAccount(account, token.getAccountType());

        // 2. 模糊失败提示（防账号枚举）
        if (user == null || rawPassword == null
                || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.info("[IAM] 认证失败: account={} masked", mask(account));
            throw new BadCredentialsException("Bad credentials");
        }

        // 3. 账户状态校验
        if (!user.canLogin()) {
            log.warn("[IAM] 账户状态不可登录: userId={} status={}", user.getId(), user.getStatus());
            // 这里先统一 DisabledException；后续如果 status 有更细粒度（LOCKED/EXPIRED 等）
            // 可在此处分支抛 LockedException/CredentialsExpiredException
            throw new DisabledException("User is disabled");
        }

        // 4. 构造已认证 Token：principal = 领域 User（DDD 纯净，不实现 UserDetails
        AccountAuthenticationToken authenticated = new AccountAuthenticationToken(
                user, null, java.util.Collections.emptyList(), token.getAccountType());

        log.debug("[IAM] 认证成功: userId={}", user.getId());
        return authenticated;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AccountAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static String mask(String account) {
        if (account == null || account.length() <= 2) return "**";
        return account.charAt(0) + "***" + account.charAt(account.length() - 1);
    }
}
