package com.acme.test;

import io.quarkus.elytron.security.common.BcryptUtil;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        String password = "JeyselDePaula";
        String passwordHash = BcryptUtil.bcryptHash(password);
        System.out.println(passwordHash);
    }
}
