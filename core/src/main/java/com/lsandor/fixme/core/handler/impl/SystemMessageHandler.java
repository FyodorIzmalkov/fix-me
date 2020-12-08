package com.lsandor.fixme.core.handler.impl;

import com.lsandor.fixme.core.handler.AbstractMessageHandler;

import java.nio.channels.AsynchronousSocketChannel;

import static com.lsandor.fixme.core.utils.Constants.SYSTEM_MESSAGE;

public class SystemMessageHandler extends AbstractMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        if (!message.startsWith(SYSTEM_MESSAGE)) {
            super.handle(channel, message);
        }
    }
}
