package com.lsandor.fixme.router.handler;

import com.lsandor.fixme.core.handler.AbstractMessageHandler;
import com.lsandor.fixme.core.model.MessageToSend;
import com.lsandor.fixme.router.map.RouterMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import static com.lsandor.fixme.core.Core.getFixValueFromMessageByTag;
import static com.lsandor.fixme.core.messenger.Messenger.sendSystemMessage;
import static com.lsandor.fixme.core.tags.FIX_tag.TARGET_ID;
import static com.lsandor.fixme.core.tags.FIX_tag.TARGET_NAME;

@Slf4j
@RequiredArgsConstructor
public class MessageRouter extends AbstractMessageHandler {

    private final RouterMap routerMap;
    private final Map<String, Set<String>> failedMessages;
    private final BlockingQueue<MessageToSend> messagesQueue;

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        log.info("Sending message: {}", message);

        String targetId = getFixValueFromMessageByTag(message, TARGET_ID);
        String targetName = getFixValueFromMessageByTag(message, TARGET_NAME);
        AsynchronousSocketChannel channelToSend = routerMap.tryToGetChannelByNameOrId(targetName, targetId);

        if (channelToSend != null && channelToSend.isOpen()) {
            try {
                messagesQueue.put(MessageToSend.builder()
                        .channel(channelToSend)
                        .message(message)
                        .build());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            super.handle(channel, message);
        } else {
            sendSystemMessage(channel, "No client found with id: " + targetId + " and name: " + targetName + " we will try to resend message later.");
            log.info("Current routingMap: {}", routerMap.toString());
            failedMessages.computeIfAbsent(targetName, (key) -> new HashSet<>()).add(message);
        }
    }
}
