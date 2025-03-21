package com.example.movie_booking_system.config;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;

public class KeyConfig {
    public static final Key KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    static String encodedKey = Base64.getEncoder().encodeToString(KEY.getEncoded());

    static byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
    static SecretKey key = Keys.hmacShaKeyFor(keyBytes);
}

