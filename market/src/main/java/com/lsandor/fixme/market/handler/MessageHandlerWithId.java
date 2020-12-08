package com.lsandor.fixme.market.handler;

import com.lsandor.fixme.core.Core;
import com.lsandor.fixme.core.tags.FIX_tag;
import com.lsandor.fixme.core.Result;
import com.lsandor.fixme.core.messenger.Messenger;
import com.lsandor.fixme.core.db.Database;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;

import java.nio.channels.AsynchronousSocketChannel;

public abstract class MessageHandlerWithId extends AbstractMessageHandler {

    private final String id;
    private final String name;

    public MessageHandlerWithId(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected void rejectedMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        sendMessage(clientChannel, fixMessage, message, Result.Rejected);
    }

    protected void executedMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        sendMessage(clientChannel, fixMessage, message, Result.Executed);
    }

    private void sendMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message, Result result) {
        final String targetName = Core.getFixValueByTag(fixMessage, FIX_tag.SOURCE_NAME);
        if (isInsertMessagesToDb()) {
            Database.insert(
                    name,
                    targetName,
                    Core.getFixValueByTag(fixMessage, FIX_tag.TYPE),
                    Core.getFixValueByTag(fixMessage, FIX_tag.INSTRUMENT),
                    Core.getFixValueByTag(fixMessage, FIX_tag.PRICE),
                    Core.getFixValueByTag(fixMessage, FIX_tag.QUANTITY),
                    result.toString(),
                    message);
            Database.selectAll();
        }
        Messenger.sendMessage(clientChannel, Core.resultFixMessage(message, id, name, targetName, result));
    }

    protected boolean isInsertMessagesToDb() {
        return false;
    }
}
