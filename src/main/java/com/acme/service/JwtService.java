package com.acme.service;

import com.acme.domain.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(
            name = "smallrye.jwt.new-token.lifespan",
            defaultValue = "3600"
    )
    long expiresInSeconds;

    public String generate(User user) {
        return Jwt.upn(user.getEmail())
                .subject(user.getId().toString())
                .claim(
                        "familyId",
                        user.getFamily().getId().toString()
                )
                .groups(Set.of(user.getRole().name()))
                .sign();
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
