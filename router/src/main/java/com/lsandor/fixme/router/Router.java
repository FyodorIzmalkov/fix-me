package com.lsandor.fixme.router;

import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.handler.impl.MandatoryTagsValidator;
import com.lsandor.fixme.core.handler.impl.MessageChecksumValidator;
import com.lsandor.fixme.core.handler.impl.SystemMessageHandler;
import com.lsandor.fixme.core.model.MessageToSend;
import com.lsandor.fixme.router.handler.MessageSender;
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
    private final Map<String, AsynchronousSocketChannel> routingMap = new ConcurrentHashMap<>();
    private final Map<String, List<String>> failedMessages = new ConcurrentHashMap<>();
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

        while (true) {
            // работаем пока не вырубят)
        }
    }

    private AsynchronousServerSocketChannel createAsynchronousServerSocketChannel(int port, MessageHandler messageHandler) {
        AsynchronousServerSocketChannel serverListener = null;
        try {
            serverListener = AsynchronousServerSocketChannel
                    .open()
                    .bind(new InetSocketAddress(LOCALHOST, port));
            serverListener.accept(null, new CompletionHandlerImpl(serverListener, routingMap, id, messageHandler));
        } catch (IOException e) {
            log.error("Could not open the socket: {}", e.getLocalizedMessage());
        }

        return serverListener;
    }

    private void sendFailedMessages() {
        if (failedMessages.isEmpty()) {
            return;
        }

        log.info("Sending failed messages.");
        failedMessages.keySet().removeIf(nameOfTarget -> {
            AsynchronousSocketChannel channel = routingMap.get(nameOfTarget);
            if (channel != null) {
                log.info("Sending message to {}", nameOfTarget);
                List<String> messagesList = failedMessages.get(nameOfTarget);
                messagesList.forEach(msg -> sendMessage(channel, msg));
                return true;
            }
            return false;
        });

    }

    private MessageHandler createMessageHandler() {
        MessageHandler messageHandler = new SystemMessageHandler();
        MessageHandler mandatoryTagsValidator = new MandatoryTagsValidator();
        MessageHandler checksumValidator = new MessageChecksumValidator();
        MessageHandler messageSender = new MessageSender(routingMap, failedMessages, messagesQueue);

        messageHandler.setNextHandler(mandatoryTagsValidator);
        mandatoryTagsValidator.setNextHandler(checksumValidator);
        checksumValidator.setNextHandler(messageSender);
        return messageHandler;
    }
}
