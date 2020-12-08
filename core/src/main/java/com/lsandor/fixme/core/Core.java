package com.lsandor.fixme.core;


import com.lsandor.fixme.core.exception.UserInputValidationException;
import com.lsandor.fixme.core.exception.WrongFixTagException;
import com.lsandor.fixme.core.tags.FIX_tag;
import com.lsandor.fixme.core.utils.Constants;

import static com.lsandor.fixme.core.utils.Constants.FIELD_DELIMITER_FOR_SPLIT;
import static com.lsandor.fixme.core.utils.Constants.tagPatternMap;

public class Core {

    public static String userInputToFixMessage(String input, String id, String name) throws UserInputValidationException {
        final String[] m = input.split(Constants.USER_INPUT_DELIMITER);
        if (m.length != 5) {
            throw new UserInputValidationException("Wrong input, should be: " + Constants.USER_MESSAGE_FORMAT);
        }
        final StringBuilder builder = new StringBuilder();
        addTag(builder, FIX_tag.ID, id);
        addTag(builder, FIX_tag.SOURCE_NAME, name);
        addTag(builder, FIX_tag.TARGET_NAME, m[0]);
        addTag(builder, FIX_tag.TYPE, m[1]);
        addTag(builder, FIX_tag.INSTRUMENT, m[2]);
        addTag(builder, FIX_tag.QUANTITY, m[3]);
        addTag(builder, FIX_tag.PRICE, m[4]);
        addTag(builder, FIX_tag.CHECKSUM, calculateChecksum(builder.toString()));
        return builder.toString();
    }

    public static String resultFixMessage(String message, String id, String srcName, String targetName, Result result) {
        final StringBuilder builder = new StringBuilder();
        addTag(builder, FIX_tag.ID, id);
        addTag(builder, FIX_tag.SOURCE_NAME, srcName);
        addTag(builder, FIX_tag.TARGET_NAME, targetName);
        addTag(builder, FIX_tag.RESULT, result.toString());
        addTag(builder, FIX_tag.MESSAGE, message);
        addTag(builder, FIX_tag.CHECKSUM, calculateChecksum(builder.toString()));
        return builder.toString();
    }

    private static void addTag(StringBuilder builder, FIX_tag tag, String value) {
        builder.append(tag.getValue())
                .append(Constants.TAG_DELIMITER)
                .append(value)
                .append(Constants.FIELD_DELIMITER);
    }

    public static String calculateChecksum(String message) {
        final byte[] bytes = message.getBytes();
        int sum = 0;
        for (byte aByte : bytes) {
            sum += aByte;
        }
        return String.format("%03d", sum % 256);
    }

    public static String getMessageWithoutChecksum(String fixMessage) {
        final int checksumIndex = fixMessage.lastIndexOf(FIX_tag.CHECKSUM.getValue() + Constants.TAG_DELIMITER);
        return fixMessage.substring(0, checksumIndex);
    }

    public static String getFixValueByTag(String fixMessage, FIX_tag tag) {
        String[] splitMessage = fixMessage.split(FIELD_DELIMITER_FOR_SPLIT);
        String startPattern = tagPatternMap.get(tag);
        for (String partOfMessage : splitMessage) {
            if (partOfMessage.startsWith(startPattern)) {
                return partOfMessage.substring(startPattern.length());
            }
        }
        throw new WrongFixTagException("No '" + tag + "' tag in message + '" + fixMessage + "'");
    }
}
