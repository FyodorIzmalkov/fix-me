package com.lsandor.fixme.broker.handler;


import com.lsandor.fixme.core.Core;
import com.lsandor.fixme.core.tags.FIX_tag;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;

import java.nio.channels.AsynchronousSocketChannel;

public class ExecutionResult extends AbstractMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        final String result = Core.getFixValueFromMessageByTag(message, FIX_tag.RESULT);
        final String resultMessage = Core.getFixValueFromMessageByTag(message, FIX_tag.MESSAGE);
        System.out.println("Operation result: " + result + " - " + resultMessage);
        super.handle(channel, message);
    }
}
