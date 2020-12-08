package com.lsandor.fixme.router;

import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.handler.impl.ChecksumValidator;
import com.lsandor.fixme.core.handler.impl.InternalMessageHandler;
import com.lsandor.fixme.core.handler.impl.MandatoryTagsValidator;
import com.lsandor.fixme.router.handler.MessageProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lsandor.fixme.core.messenger.Messenger.sendMessage;
import static com.lsandor.fixme.core.utils.Constants.*;

@Slf4j
public class Router {

    private static final long DELAY_BETWEEN_RESENDING_MESSAGES = 10L;
    private final Map<String, AsynchronousSocketChannel> routingMap = new ConcurrentHashMap<>();
    private final Map<String, String> failedMessages = new ConcurrentHashMap<>();
    private final AtomicInteger id = new AtomicInteger(1);
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public void runServer() {
        log.info("Router started");

        MessageHandler messageHandler = getMessageHandler();
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
            serverListener.accept(null, new ClientCompletionHandler(serverListener, routingMap, id, messageHandler));
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
                sendMessage(channel, failedMessages.get(nameOfTarget));
                return true;
            }
            return false;
        });

    }

    private MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = new InternalMessageHandler();
        final MessageHandler mandatoryTagsValidator = new MandatoryTagsValidator();
        final MessageHandler checksumValidator = new ChecksumValidator();
        final MessageHandler messageParser = new MessageProcessor(routingMap, failedMessages);
        messageHandler.setNextHandler(mandatoryTagsValidator);
        mandatoryTagsValidator.setNextHandler(checksumValidator);
        checksumValidator.setNextHandler(messageParser);
        return messageHandler;
    }
}
