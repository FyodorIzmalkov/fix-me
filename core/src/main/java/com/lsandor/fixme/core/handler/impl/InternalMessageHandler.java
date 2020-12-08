package com.lsandor.fixme.core.handler.impl;

import com.lsandor.fixme.core.handler.AbstractMessageHandler;
import com.lsandor.fixme.core.utils.Constants;

import java.nio.channels.AsynchronousSocketChannel;

public class InternalMessageHandler extends AbstractMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        if (!message.startsWith(Constants.INTERNAL_MESSAGE)) {
            super.handle(channel, message);
        }
    }
}
