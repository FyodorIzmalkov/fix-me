package com.lsandor.fixme.core.handler;

import lombok.Setter;

import java.nio.channels.AsynchronousSocketChannel;

public abstract class AbstractMessageHandler implements MessageHandler {

    @Setter
    private MessageHandler nextHandler;

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        if (nextHandler != null) {
            nextHandler.handle(channel, message);
        }
    }
}
