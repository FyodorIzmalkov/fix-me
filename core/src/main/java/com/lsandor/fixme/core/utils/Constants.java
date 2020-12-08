package com.lsandor.fixme.core.utils;

import com.lsandor.fixme.core.tags.FIX_tag;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class Constants {
    public static final String LOCALHOST = "127.0.0.1";
    public static final String FORMAT_FOR_ID = "%06d";
    public static final String USER_MESSAGE_FORMAT = "'MARKET_NAME BUY_OR_SELL INSTRUMENT_NAME QUANTITY PRICE'";
    public static final String USER_INPUT_DELIMITER = " ";
    public static final String TAG_DELIMITER = "=";
    public static final String FIELD_DELIMITER = "|";
    public static final String FIELD_DELIMITER_FOR_SPLIT = Pattern.quote(Constants.FIELD_DELIMITER);
    public static final String SYSTEM_MESSAGE = "SYSTEM_MESSAGE:";
    public static final String EMPTY_STRING = "";
    public static final String SHA_256 = "SHA-256";
    public static final String MARKET_NAME_PREFIX = "Market_";

    public static final int BROKER_PORT = 5000;
    public static final int MARKET_PORT = 5001;
    public static final int DEFAULT_BUFFER_SIZE = 2048;
    public static final int ZERO_INDEX = 0;
    public static final int DEFAULT_QUEUE_CAPACITY = 500;
    public static final int EXECUTOR_THREADS = 5;

    public static final Map<FIX_tag, String> tagPatternMap = new ConcurrentHashMap<>();

    static {
        FIX_tag[] fix_tags = FIX_tag.values();
        synchronized (tagPatternMap) {
            for (FIX_tag fixTag : fix_tags) {
                tagPatternMap.put(fixTag, fixTag.getValue() + TAG_DELIMITER);
            }
        }
    }
}
