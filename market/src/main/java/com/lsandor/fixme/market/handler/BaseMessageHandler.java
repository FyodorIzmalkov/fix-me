package com.lsandor.fixme.market.handler;

import com.lsandor.fixme.core.db.Database;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;
import com.lsandor.fixme.core.status.Status;
import com.lsandor.fixme.core.tags.FIX_tag;
import lombok.RequiredArgsConstructor;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

import static com.lsandor.fixme.core.Core.*;
import static com.lsandor.fixme.core.messenger.Messenger.sendMessage;
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
        String targetid = fixValueMap.get(SOURCE_ID); //TODO COULD BE ERROR
        if (saveTransactionToDatabase()) {
            Database.insert(
                    name,
                    targetid,
                    fixValueMap.get(TYPE),
                    fixValueMap.get(INSTRUMENT),
                    fixValueMap.get(PRICE),
                    fixValueMap.get(QUANTITY),
                    status.toString(),
                    message);
            Database.selectAll();
        }
        sendMessage(clientChannel, resultFixMessage(message, id, name, targetid, status));
    }

    protected boolean saveTransactionToDatabase() {
        return false;
    }

    private String resultFixMessage(String message, String id, String sourceName, String targetName, Status status) {
        StringBuilder builder = new StringBuilder();
        addPart(builder, SOURCE_ID, id);
        addPart(builder, SOURCE_NAME, sourceName);
        addPart(builder, TARGET_ID, targetName);
        addPart(builder, STATUS, status.name());
        addPart(builder, MESSAGE, message);
        addPart(builder, CHECKSUM, calculateChecksumFromString(builder.toString()));

        return builder.toString();
    }
}
