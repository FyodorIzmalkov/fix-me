package com.lsandor.fixme.core.utils;

import com.lsandor.fixme.core.tags.FIX_tag;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class Constants {
    public static final String LOCALHOST = "127.0.0.1";
    public static final String FORMAT_FOR_ID = "%06d";
    public static final String USER_INPUT_FORMAT = "MARKET_ID MARKET_NAME BUY_OR_SELL INSTRUMENT_NAME QUANTITY PRICE";
    public static final String TAG_DELIMITER = "=";
    public static final String FIELD_DELIMITER = "|";
    public static final String FIELD_DELIMITER_FOR_SPLIT = Pattern.quote(Constants.FIELD_DELIMITER);
    public static final String SYSTEM_MESSAGE = "SYSTEM_MESSAGE:";
    public static final String EXAMPLE_MESSAGE_FOR_BROKER = "000001 Market_No04648 BUY EUR_USD 10000 10";
    public static final String EMPTY_STRING = "";
    public static final String SHA_256 = "SHA-256";
    public static final String MARKET_NAME_PREFIX = "Market_";
    public static final String BROKER_NAME_PREFIX = "Broker_";
    public static final String ZERO_ID = "000000";

    public static final int BROKER_PORT = 5000;
    public static final int MARKET_PORT = 5001;
    public static final int DEFAULT_BUFFER_SIZE = 2048;
    public static final int ZERO_INDEX = 0;
    public static final int DEFAULT_QUEUE_CAPACITY = 500;
    public static final int EXECUTOR_THREADS = 5;

    public static final int TARGET_ID_NUM = 0;
    public static final int TARGET_NAME_NUM = 1;
    public static final int TYPE_NUM = 2;
    public static final int INSTRUMENT_NUM = 3;
    public static final int QUANTITY_NUM = 4;
    public static final int PRICE_NUM = 5;

    public static final Map<FIX_tag, String> tagPatternMap = new ConcurrentHashMap<>();         // fixTag to fixTag.getValue() + TAG_DELIMITER
    public static final Map<String, FIX_tag> reverseTagPatternMap = new ConcurrentHashMap<>(); // fixTag.getValue() + TAG_DELIMITER to fixTag

    static {
        FIX_tag[] fix_tags = FIX_tag.values();
        synchronized (tagPatternMap) {
            for (FIX_tag fixTag : fix_tags) {
                tagPatternMap.put(fixTag, fixTag.getValue() + TAG_DELIMITER);
                reverseTagPatternMap.put(fixTag.getValue() + TAG_DELIMITER, fixTag);
            }
        }

    }
}
