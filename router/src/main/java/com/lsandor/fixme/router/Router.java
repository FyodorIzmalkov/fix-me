package com.lsandor.fixme.router;

import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.handler.impl.MandatoryTagsValidator;
import com.lsandor.fixme.core.handler.impl.MessageChecksumValidator;
import com.lsandor.fixme.core.handler.impl.SystemMessageHandler;
import com.lsandor.fixme.core.model.MessageToSend;
import com.lsandor.fixme.router.completion.handler.RouterCompletionHandlerImpl;
import com.lsandor.fixme.router.handler.MessageRouter;
import com.lsandor.fixme.router.map.RouterMap;
import com.lsandor.fixme.router.processor.MessagesQueueProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lsandor.fixme.core.messenger.Messenger.sendMessage;
import static com.lsandor.fixme.core.utils.Constants.*;

@Slf4j
public class Router {

    private static final long DELAY_BETWEEN_RESENDING_MESSAGES = 10L;
    private final RouterMap routerMap = new RouterMap();
    private final Map<String, List<String>> failedMessages = new ConcurrentHashMap<>();
    private final AtomicInteger id = new AtomicInteger(1);
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final BlockingQueue<MessageToSend> messagesQueue = new ArrayBlockingQueue<>(DEFAULT_QUEUE_CAPACITY);

    public void runServer() {
        log.info("Router started");

        MessagesQueueProcessor messagesQueueProcessor = new MessagesQueueProcessor(messagesQueue);
        messagesQueueProcessor.startSendingMessages();
        createAsynchronousServerSocketChannel(BROKER_PORT, createMessageHandler());
        createAsynchronousServerSocketChannel(MARKET_PORT, createMessageHandler());
        executorService.scheduleAtFixedRate(this::sendFailedMessages, 0L, DELAY_BETWEEN_RESENDING_MESSAGES, TimeUnit.SECONDS);
    }

    private void createAsynchronousServerSocketChannel(int port, MessageHandler messageHandler) {
        try {
            AsynchronousServerSocketChannel serverListener = AsynchronousServerSocketChannel
                    .open()
                    .bind(new InetSocketAddress(LOCALHOST, port));
            serverListener.accept(null, new RouterCompletionHandlerImpl(routerMap, serverListener, id, messageHandler));
        } catch (IOException e) {
            log.error("Could not open the socket: {}", e.getLocalizedMessage());
        }
    }

    private void sendFailedMessages() {
        if (failedMessages.isEmpty()) {
            return;
        }

        log.info("Trying to send failed messages.");
        failedMessages.keySet().removeIf(targetName -> {
            AsynchronousSocketChannel channel = routerMap.tryToGetChannel(targetName);
            if (channel != null && channel.isOpen()) {
                log.info("Sending message to {}", targetName);
                List<String> messagesList = failedMessages.get(targetName);
                String message = messagesList.remove(0);
                sendMessage(channel, message);

                return messagesList.isEmpty();
            }
            return false;
        });
    }

    private MessageHandler createMessageHandler() {
        MessageHandler messageHandler = new SystemMessageHandler();
        MessageHandler mandatoryTagsValidator = new MandatoryTagsValidator();
        MessageHandler checksumValidator = new MessageChecksumValidator();
        MessageHandler messageSender = new MessageRouter(routerMap, failedMessages, messagesQueue);

        messageHandler.setNextHandler(mandatoryTagsValidator);
        mandatoryTagsValidator.setNextHandler(checksumValidator);
        checksumValidator.setNextHandler(messageSender);
        return messageHandler;
    }
}
