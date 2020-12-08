package com.lsandor.fixme.core.handler.impl;

import com.lsandor.fixme.core.Core;
import com.lsandor.fixme.core.tags.FIX_tag;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;
import com.lsandor.fixme.core.messenger.Messenger;
import com.lsandor.fixme.core.exception.WrongFixTagException;

import java.nio.channels.AsynchronousSocketChannel;

public class MandatoryTagsValidator extends AbstractMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        try {
            String sourceId = Core.getFixValueByTag(message, FIX_tag.ID);
            Core.getFixValueByTag(message, FIX_tag.SOURCE_NAME);
            Core.getFixValueByTag(message, FIX_tag.TARGET_NAME);
            String checksum = Core.getFixValueByTag(message, FIX_tag.CHECKSUM);

            Integer.parseInt(sourceId);
            Integer.parseInt(checksum);
            super.handle(channel, message);
        } catch (WrongFixTagException ex) {
            Messenger.sendInternalMessage(channel, ex.getMessage());
        } catch (NumberFormatException ex) {
            Messenger.sendInternalMessage(channel, "SOURCE_ID, CHECKSUM Tags should be numbers: " + message);
        }
    }
}
