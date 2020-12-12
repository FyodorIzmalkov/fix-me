package com.lsandor.fixme.core;


import com.lsandor.fixme.core.exception.NoFixTagException;
import com.lsandor.fixme.core.tags.FIX_tag;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.lsandor.fixme.core.tags.FIX_tag.CHECKSUM;
import static com.lsandor.fixme.core.utils.Constants.*;

public class Core {

    public static String calculateChecksumFromString(String fixMessage) {
        return getSHA256Hash(fixMessage);
    }

    private static String getSHA256Hash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "NO_HASH";
    }

    private static String bytesToHex(byte[] hash) {
        return DatatypeConverter.printHexBinary(hash).toUpperCase();
    }

    public static String getFixMessageWithoutChecksum(String fixMessage) {
        return fixMessage.substring(ZERO_INDEX, fixMessage.lastIndexOf(tagPatternMap.get(CHECKSUM)));
    }

    public static String getFixValueFromMessageByTag(String fixMessage, FIX_tag tag) {
        String[] splitMessage = fixMessage.split(FIELD_DELIMITER_FOR_SPLIT);
        String startPattern = tagPatternMap.get(tag);
        for (String partOfMessage : splitMessage) {
            if (partOfMessage.startsWith(startPattern)) {
                return partOfMessage.substring(startPattern.length());
            }
        }

        throw new NoFixTagException("No mandatory tag: " + tag.toString() + " in the message: " + fixMessage);
    }

    public static Map<FIX_tag, String> getFixMapByTag(String fixMessage) {
        String[] splitMessage = fixMessage.split(FIELD_DELIMITER_FOR_SPLIT);
        Set<String> set = reverseTagPatternMap.keySet();
        Map<FIX_tag, String> result = new HashMap<>();
        for (String partOfMessage : splitMessage) {
            for (String startOfTagPattern : set) {
                if (partOfMessage.startsWith(startOfTagPattern)) {
                    result.put(reverseTagPatternMap.get(startOfTagPattern), partOfMessage.substring(startOfTagPattern.length()));
                    break;
                }
            }
        }
        return result;
    }

    public static void addPart(StringBuilder resultBuilder, FIX_tag fixTag, String tagMessage) {
        resultBuilder.append(fixTag.getValue())
                .append(TAG_DELIMITER)
                .append(tagMessage)
                .append(FIELD_DELIMITER);
    }
}
