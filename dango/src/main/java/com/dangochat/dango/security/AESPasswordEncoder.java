package com.dangochat.dango.security;

import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AESPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        try {
            return AESUtil.encrypt(rawPassword.toString());
        } catch (Exception e) {
            throw new RuntimeException("Password encryption failed", e);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encryptedPassword) {
        try {
            // String decryptedPassword = AESUtil.decrypt(encryptedPassword);
            // log.debug("password test : {} /// {} /// {}", rawPassword, encryptedPassword, decryptedPassword);
            return rawPassword.toString().equals(encryptedPassword);
        } catch (Exception e) {
            throw new RuntimeException("Password decryption failed", e);
        }
    }
}
