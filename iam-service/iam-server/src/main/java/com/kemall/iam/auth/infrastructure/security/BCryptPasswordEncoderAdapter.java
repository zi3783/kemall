package com.kemall.iam.auth.infrastructure.security;

import com.kemall.iam.auth.application.port.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {

    private final org.springframework.security.crypto.password.PasswordEncoder delegate;

    public BCryptPasswordEncoderAdapter() {
        this.delegate =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(
            String rawPassword,
            String encodedPassword) {

        return delegate.matches(rawPassword, encodedPassword);
    }


}

class main{
    public static void main(String[] args) {
        BCryptPasswordEncoderAdapter e = new BCryptPasswordEncoderAdapter();
        String encode = e.encode("123456");
        System.out.println(encode);
    }
}