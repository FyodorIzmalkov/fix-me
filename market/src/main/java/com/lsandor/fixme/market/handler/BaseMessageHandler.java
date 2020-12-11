package com.lsandor.fixme.market.handler;

import com.lsandor.fixme.core.Core;
import com.lsandor.fixme.core.db.Database;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;
import com.lsandor.fixme.core.messenger.Messenger;
import com.lsandor.fixme.core.status.Status;
import com.lsandor.fixme.core.tags.FIX_tag;
import lombok.RequiredArgsConstructor;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

import static com.lsandor.fixme.core.Core.getFixMapByTag;
import static com.lsandor.fixme.core.tags.FIX_tag.*;

@RequiredArgsConstructor
public abstract class BaseMessageHandler extends AbstractMessageHandler {

    private final String id;
    private final String name;

    protected void responseWithStatusRejected(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        sendMessageWithStatus(clientChannel, fixMessage, message, Status.REJECTED);
    }

    protected void responseWithStatusExecuted(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        sendMessageWithStatus(clientChannel, fixMessage, message, Status.EXECUTED);
    }

    private void sendMessageWithStatus(AsynchronousSocketChannel clientChannel, String fixMessage, String message, Status status) {
        Map<FIX_tag, String> fixValueMap = getFixMapByTag(fixMessage);
//        String targetName = Core.getFixValueFromMessageByTag(fixMessage, SOURCE_NAME); //TODO COULD BE ERROR
        String targetName = fixValueMap.get(SOURCE_NAME); //TODO COULD BE ERROR
        if (saveTransactionToDatabase()) {
            Database.insert(
                    name,
                    targetName,
                    fixValueMap.get(TYPE),
                    fixValueMap.get(INSTRUMENT),
                    fixValueMap.get(PRICE),
                    fixValueMap.get(QUANTITY),
                    status.toString(),
                    message);
            Database.selectAll();
        }
        Messenger.sendMessage(clientChannel, Core.resultFixMessage(message, id, name, targetName, status));
    }

    protected boolean saveTransactionToDatabase() {
        return false;
    }
}
