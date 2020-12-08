package com.lsandor.fixme.core.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Utils {

    private static final String[] INSTRUMENTS = {
            "bolt", "nail", "screwdriver", "screw",
            "hammer", "saw", "drill", "wrench", "knife",
            "scissors", "toolbox", "tape", "needle"
    };

    public static Map<String, Integer> getRandomInstruments() {
        final Map<String, Integer> instruments = new HashMap<>();
        final Random random = new Random();
        for (String instrument : INSTRUMENTS) {
            if (random.nextBoolean()) {
                instruments.put(instrument, random.nextInt(9) + 1);
            }
        }
        return instruments;
    }

    public static String getClientName(String[] args) {
        return args.length == 1
                ? args[0]
                : DateTimeFormatter.ofPattern("mmss").format(LocalDateTime.now());
    }

    public static boolean isParsableToInt(String strToParse) {
        return strToParse.matches("\\d+");
    }
}
