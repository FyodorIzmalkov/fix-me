package com.lsandor.fixme.core.handler.impl;

import com.lsandor.fixme.core.exception.NoFixTagException;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;

import java.nio.channels.AsynchronousSocketChannel;

import static com.lsandor.fixme.core.Core.getFixValueFromMessageByTag;
import static com.lsandor.fixme.core.messenger.Messenger.sendSystemMessage;
import static com.lsandor.fixme.core.tags.FIX_tag.*;
import static com.lsandor.fixme.core.utils.Utils.isParsableToInt;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MandatoryTagsValidator extends AbstractMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        try {
            String sourceId = getFixValueFromMessageByTag(message, SOURCE_ID);
            getFixValueFromMessageByTag(message, SOURCE_NAME);
            getFixValueFromMessageByTag(message, TARGET_ID);
            String checksum = getFixValueFromMessageByTag(message, CHECKSUM);

            if (isEmpty(sourceId) || !isParsableToInt(sourceId)) {
                sendSystemMessage(channel, "SOURCE_ID tag value must be number: " + message);
                return;
            }

            if (isEmpty(checksum)) {
                sendSystemMessage(channel, "CHECKSUM tag value must be filled: " + message);
                return;
            }

            super.handle(channel, message);
        } catch (NoFixTagException e) {
            sendSystemMessage(channel, e.getLocalizedMessage());
        }
    }
}
