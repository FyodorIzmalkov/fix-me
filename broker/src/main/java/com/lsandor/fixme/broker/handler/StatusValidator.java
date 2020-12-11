package com.lsandor.fixme.broker.handler;


import com.lsandor.fixme.core.exception.NoFixTagException;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;
import com.lsandor.fixme.core.status.Status;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.AsynchronousSocketChannel;

import static com.lsandor.fixme.core.Core.getFixValueFromMessageByTag;
import static com.lsandor.fixme.core.tags.FIX_tag.STATUS;
import static org.apache.commons.lang3.EnumUtils.isValidEnum;

@Slf4j
public class StatusValidator extends AbstractMessageHandler {

    @Override
    public void handle(AsynchronousSocketChannel channel, String fixMessage) {
        String status;
        try {
            status = getFixValueFromMessageByTag(fixMessage, STATUS);
        } catch (NoFixTagException e) {
            log.error(e.getMessage());
            return;
        }

        if (isValidEnum(Status.class, status)) {
            super.handle(channel, fixMessage);
        } else {
            log.error("Status type is wrong: {}", fixMessage);
        }
    }
}
