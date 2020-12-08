package com.lsandor.fixme.core.handler.impl;

import com.lsandor.fixme.core.Core;
import com.lsandor.fixme.core.tags.FIX_tag;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;
import com.lsandor.fixme.core.messenger.Messenger;

import java.nio.channels.AsynchronousSocketChannel;

public class ChecksumValidator extends AbstractMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        final String calculatedChecksum = Core.calculateChecksum(Core.getMessageWithoutChecksum(message));
        final String messageChecksum = Core.getFixValueByTag(message, FIX_tag.CHECKSUM);
        final boolean isValidChecksum = calculatedChecksum.equals(messageChecksum);
        if (isValidChecksum) {
            super.handle(channel, message);
        } else {
            Messenger.sendInternalMessage(channel, "Invalid checksum for message: " + message);
        }
    }
}
