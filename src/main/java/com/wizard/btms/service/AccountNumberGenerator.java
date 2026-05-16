package com.wizard.btms.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AccountNumberGenerator {

    public String generateAccountNumber() {

        Random random = new Random();

        return "ACC" + (100000000 + random.nextInt(900000000));
    }
}