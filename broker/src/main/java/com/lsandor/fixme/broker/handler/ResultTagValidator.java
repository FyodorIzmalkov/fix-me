package com.lsandor.fixme.broker.handler;


import com.lsandor.fixme.core.Core;
import com.lsandor.fixme.core.tags.FIX_tag;
import com.lsandor.fixme.core.Result;
import com.lsandor.fixme.core.exception.WrongFixTagException;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;

import java.nio.channels.AsynchronousSocketChannel;

public class ResultTagValidator extends AbstractMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        final String result;
        try {
            result = Core.getFixValueByTag(message, FIX_tag.RESULT);
        } catch (WrongFixTagException ex) {
            System.out.println(ex.getMessage());
            return;
        }
        if (Result.is(result)) {
            super.handle(channel, message);
        } else {
            System.out.println("Wrong result type in message: " + message);
        }
    }
}
