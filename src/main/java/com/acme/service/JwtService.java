package com.acme.service;

import com.acme.domain.model.User;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan", defaultValue = "3600")
    long expiresInSeconds;

    public String generate(User user){
        JwtClaimsBuilder claims = Jwt.upn(user.getEmail())
                .subject(user.getId().toString())
                .groups(Set.of(user.getRole().name()));

        if(user.getFamily() != null) {
            claims.claim("familyId", user.getFamily().getId().toString());
        }

        return claims.sign();
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
