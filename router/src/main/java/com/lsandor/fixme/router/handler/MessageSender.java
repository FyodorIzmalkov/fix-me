package com.lsandor.fixme.router.handler;

import com.lsandor.fixme.core.handler.AbstractMessageHandler;
import com.lsandor.fixme.core.messenger.Messenger;
import com.lsandor.fixme.core.model.MessageToSend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import static com.lsandor.fixme.core.Core.getFixValueFromMessageByTag;
import static com.lsandor.fixme.core.tags.FIX_tag.TARGET_NAME;

@Slf4j
@RequiredArgsConstructor
public class MessageSender extends AbstractMessageHandler {

    private final Map<String, AsynchronousSocketChannel> routingMap;
    private final Map<String, Set<String>> failedMessages;
    private final BlockingQueue<MessageToSend> messagesQueue;

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        log.info("Sending message: {}", message);

        String targetName = getFixValueFromMessageByTag(message, TARGET_NAME);
        AsynchronousSocketChannel channelToSend = routingMap.get(targetName);

        if (channelToSend != null) {
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
            Messenger.sendSystemMessage(channel, "No client found with name: " + targetName + ", we will try to resend message later.");
            log.info("Current routingMap: {}", routingMap.toString());
            failedMessages.computeIfAbsent(targetName, (key) -> new HashSet<>()).add(message);
        }
    }
}
