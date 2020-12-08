package com.lsandor.fixme.core.handler;

import java.nio.channels.AsynchronousSocketChannel;

public abstract class AbstractMessageHandler implements MessageHandler {

    private MessageHandler nextHandler;

    @Override
    public final void setNextHandler(MessageHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        if (nextHandler != null) {
            nextHandler.handle(channel, message);
        }
    }
}
