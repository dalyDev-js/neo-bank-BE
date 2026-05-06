package com.neobank.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;

@Component
public class AccountNumberGenerator {

    private static final String PREFIX = "NB";
    private static final Random RANDOM = new Random();

    public String generateAccountNumber() {
        long number = (long) (RANDOM.nextDouble() * 9_000_000_000L) + 1_000_000_000L;
        return PREFIX + number;
    }

    public String generateReferenceNumber() {
        String date = LocalDate.now().toString().replace("-", "");
        long number = (long) (RANDOM.nextDouble() * 900_000L) + 100_000L;
        return "TXN" + date + number;
    }
}