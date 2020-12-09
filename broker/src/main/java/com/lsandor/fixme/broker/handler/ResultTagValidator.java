package com.lsandor.fixme.broker.handler;


import com.lsandor.fixme.core.Core;
import com.lsandor.fixme.core.exception.NoFixTagException;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;
import com.lsandor.fixme.core.status.Status;
import com.lsandor.fixme.core.tags.FIX_tag;

import java.nio.channels.AsynchronousSocketChannel;

import static org.apache.commons.lang3.EnumUtils.isValidEnum;

public class ResultTagValidator extends AbstractMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        final String status;
        try {
            status = Core.getFixValueFromMessageByTag(message, FIX_tag.STATUS);
        } catch (NoFixTagException ex) {
            System.out.println(ex.getMessage());
            return;
        }

        if (isValidEnum(Status.class, status)) {
            super.handle(channel, message);
        } else {
            System.out.println("Wrong status type in message: " + message);
        }
    }
}
