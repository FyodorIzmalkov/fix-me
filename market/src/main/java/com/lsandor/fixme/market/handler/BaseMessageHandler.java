package com.lsandor.fixme.market.handler;

import com.lsandor.fixme.core.database.Database;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;
import com.lsandor.fixme.core.status.Status;
import com.lsandor.fixme.core.tags.FIX_tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

import static com.lsandor.fixme.core.Core.*;
import static com.lsandor.fixme.core.messenger.Messenger.sendMessage;
import static com.lsandor.fixme.core.tags.FIX_tag.*;

@RequiredArgsConstructor
public abstract class BaseMessageHandler extends AbstractMessageHandler {

    private final String id;
    @Getter
    private final String name;

    protected void responseWithStatusRejected(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        sendMessageWithStatus(clientChannel, fixMessage, message, Status.REJECTED);
    }

    protected void responseWithStatusExecuted(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        sendMessageWithStatus(clientChannel, fixMessage, message, Status.EXECUTED);
    }

    private void sendMessageWithStatus(AsynchronousSocketChannel clientChannel, String fixMessage, String message, Status status) {
        Map<FIX_tag, String> fixValueMap = getFixMapByTag(fixMessage);
        String targetId = fixValueMap.get(SOURCE_ID);
        String targetName = fixValueMap.get(SOURCE_NAME);
        if (saveTransactionToDatabase()) {
            Database.insertTransaction(
                    name,
                    targetId,
                    targetName,
                    fixValueMap.get(TYPE),
                    fixValueMap.get(INSTRUMENT),
                    fixValueMap.get(PRICE),
                    fixValueMap.get(QUANTITY),
                    status.toString(),
                    message);
            Database.getAllAndPrintResult();
        }
        sendMessage(clientChannel, resultFixMessage(message, targetId, targetName, status));
    }

    protected boolean saveTransactionToDatabase() {
        return false;
    }

    private String resultFixMessage(String message, String targetId, String targetName, Status status) {
        StringBuilder builder = new StringBuilder();
        addPart(builder, SOURCE_ID, id);
        addPart(builder, SOURCE_NAME, name);
        addPart(builder, TARGET_ID, targetId);
        addPart(builder, TARGET_NAME, targetName);
        addPart(builder, STATUS, status.name());
        addPart(builder, MESSAGE, message);
        addPart(builder, CHECKSUM, calculateChecksumFromString(builder.toString()));

        return builder.toString();
    }
}
