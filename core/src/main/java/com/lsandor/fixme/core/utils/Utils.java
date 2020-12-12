package com.lsandor.fixme.core.utils;

import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.handler.impl.MandatoryTagsValidator;
import com.lsandor.fixme.core.handler.impl.MessageChecksumValidator;
import com.lsandor.fixme.core.handler.impl.SystemMessageHandler;
import com.lsandor.fixme.core.model.Instrument;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    private Utils() {
    }

    public static Map<Instrument, Long> getRandomInstrumentsForTheMarket() {
        Map<Instrument, Long> instruments = new ConcurrentHashMap<>();
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        for (Instrument instrument : Instrument.values()) {
            if (threadLocalRandom.nextBoolean()) {
                instruments.put(instrument, (threadLocalRandom.nextLong(2, 10) * 1_000_000L));
            }
        }
        return instruments;
    }

    public static String getOrGenerateClientName(String[] args) {
        if (args.length == 1) {
            return args[0];
        } else {
            LocalDateTime localDateTime = LocalDateTime.now();
            String hour = String.valueOf(localDateTime.getHour());
            String min = String.valueOf(localDateTime.getMinute());
            String secs = String.valueOf(localDateTime.getSecond());
            return "No".concat(hour).concat(min).concat(secs);
        }
    }

    public static boolean isParsableToInt(String strToParse) {
        return strToParse.matches("\\d+");
    }
}
