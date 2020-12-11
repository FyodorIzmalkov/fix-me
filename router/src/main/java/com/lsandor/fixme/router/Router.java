package com.lsandor.fixme.router;

import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.model.MessageToSend;
import com.lsandor.fixme.router.completion.handler.RouterCompletionHandlerImpl;
import com.lsandor.fixme.router.handler.MessageSender;
import com.lsandor.fixme.router.processor.MessagesQueueProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lsandor.fixme.core.messenger.Messenger.sendMessage;
import static com.lsandor.fixme.core.utils.Constants.*;
import static com.lsandor.fixme.core.utils.Utils.createCommonMessageHandler;

@Slf4j
public class Router {

    private static final long DELAY_BETWEEN_RESENDING_MESSAGES = 10L;
    private final Map<String, AsynchronousSocketChannel> routingMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> mapOfFailedTargetConnections = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> failedMessages = new ConcurrentHashMap<>();
    private final AtomicInteger id = new AtomicInteger(1);
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final BlockingQueue<MessageToSend> messagesQueue = new ArrayBlockingQueue<>(DEFAULT_QUEUE_CAPACITY);

    public void runServer() {
        log.info("Router started");

        MessageHandler messageHandler = createMessageHandler();
        MessagesQueueProcessor messagesQueueProcessor = new MessagesQueueProcessor(messagesQueue);
        messagesQueueProcessor.startSendingMessages();
        AsynchronousServerSocketChannel brokerListener = createAsynchronousServerSocketChannel(BROKER_PORT, messageHandler);
        AsynchronousServerSocketChannel marketsListener = createAsynchronousServerSocketChannel(MARKET_PORT, messageHandler);
        executorService.scheduleAtFixedRate(this::sendFailedMessages, 0L, DELAY_BETWEEN_RESENDING_MESSAGES, TimeUnit.SECONDS);
    }

    private AsynchronousServerSocketChannel createAsynchronousServerSocketChannel(int port, MessageHandler messageHandler) {
        AsynchronousServerSocketChannel serverListener = null;
        try {
            serverListener = AsynchronousServerSocketChannel
                    .open()
                    .bind(new InetSocketAddress(LOCALHOST, port));
            serverListener.accept(null, new RouterCompletionHandlerImpl(serverListener, routingMap, id, messageHandler));
        } catch (IOException e) {
            log.error("Could not open the socket: {}", e.getLocalizedMessage());
        }

        return serverListener;
    }

    private void sendFailedMessages() {
        if (failedMessages.isEmpty()) {
            return;
        }

        log.info("Trying to send failed messages.");
        failedMessages.keySet().removeIf(nameOfTarget -> {
            AsynchronousSocketChannel channel = routingMap.get(nameOfTarget);
            if (channel != null) {
                log.info("Sending message to {}", nameOfTarget);
                Set<String> messagesSet = failedMessages.get(nameOfTarget);
                messagesSet.forEach(msg -> sendMessage(channel, msg));
                mapOfFailedTargetConnections.remove(nameOfTarget);
                return true;
            }

            mapOfFailedTargetConnections.computeIfPresent(nameOfTarget, (k, oldVal) -> ++oldVal);
            mapOfFailedTargetConnections.computeIfAbsent(nameOfTarget, (k) -> 1);

            if (mapOfFailedTargetConnections.get(nameOfTarget) == 5) {
                mapOfFailedTargetConnections.remove(nameOfTarget);
                failedMessages.remove(nameOfTarget);
                log.info("{} removed from routing map after 5 connection attempts", nameOfTarget);
                log.info(routingMap.toString());
            }
            return false;
        });

    }

    private MessageHandler createMessageHandler() {
        MessageHandler messageHandler = createCommonMessageHandler();
        MessageHandler messageSender = new MessageSender(routingMap, failedMessages, messagesQueue);

        messageHandler.setNextHandler(messageSender);
        return messageHandler;
    }
}
