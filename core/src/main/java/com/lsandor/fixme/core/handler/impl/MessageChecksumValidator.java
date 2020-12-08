package com.lsandor.fixme.core.handler.impl;

import com.lsandor.fixme.core.exception.NoFixTagException;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;

import java.nio.channels.AsynchronousSocketChannel;

import static com.lsandor.fixme.core.Core.*;
import static com.lsandor.fixme.core.messenger.Messenger.sendSystemMessage;
import static com.lsandor.fixme.core.tags.FIX_tag.CHECKSUM;

public class MessageChecksumValidator extends AbstractMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        try {
            String calculatedChecksum = calculateChecksumFromString(getFixMessageWithoutChecksum(message));
            String checksumInMessage = getFixValueFromMessageByTag(message, CHECKSUM);

            if (calculatedChecksum.equals(checksumInMessage)) {
                super.handle(channel, message);
            } else {
                sendSystemMessage(channel, "Invalid checksum in message: " + message);
            }

        } catch (NoFixTagException e) {
            sendSystemMessage(channel, e.getLocalizedMessage());
        }
    }
}
