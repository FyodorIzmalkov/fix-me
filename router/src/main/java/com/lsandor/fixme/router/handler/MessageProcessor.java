package com.lsandor.fixme.router.handler;

import com.lsandor.fixme.core.Core;
import com.lsandor.fixme.core.tags.FIX_tag;
import com.lsandor.fixme.core.messenger.Messenger;
import com.lsandor.fixme.core.handler.AbstractMessageHandler;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class MessageProcessor extends AbstractMessageHandler {

    private final Map<String, AsynchronousSocketChannel> routingTable;
    private final Map<String, String> failedMessages;

    public MessageProcessor(Map<String, AsynchronousSocketChannel> routingTable,
                            Map<String, String> failedMessages) {
        this.routingTable = routingTable;
        this.failedMessages = failedMessages;
    }

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        System.out.println("Processing message: " + message);
        final String targetName = Core.getFixValueByTag(message, FIX_tag.TARGET_NAME);
        final AsynchronousSocketChannel targetChannel = routingTable.get(targetName);
        if (targetChannel != null) {
            Messenger.sendMessage(targetChannel, message);
            super.handle(channel, message);
        } else {
            Messenger.sendInternalMessage(channel,
                    "No connected client with such name: " + targetName + ", will try later");
            failedMessages.put(targetName, message);
        }
    }
}
