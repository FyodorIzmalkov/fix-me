package com.lsandor.fixme.core.handler;

import java.nio.channels.AsynchronousSocketChannel;

public interface MessageHandler {

    void setNextHandler(MessageHandler nextHandler);

    void handle(AsynchronousSocketChannel channel, String message);
}
