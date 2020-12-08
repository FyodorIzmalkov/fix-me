package com.lsandor.fixme.router;

import com.lsandor.fixme.core.handler.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lsandor.fixme.core.messenger.Messenger.readMessage;
import static com.lsandor.fixme.core.messenger.Messenger.sendMessage;
import static com.lsandor.fixme.core.utils.Constants.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@RequiredArgsConstructor
class CompletionHandlerImpl implements CompletionHandler<AsynchronousSocketChannel, Object> {

    private final ExecutorService executorService = Executors.newFixedThreadPool(EXECUTOR_THREADS);
    private final AsynchronousServerSocketChannel clientListener;
    private final Map<String, AsynchronousSocketChannel> routingMap;
    private final AtomicInteger atomicId;
    private final MessageHandler messageHandler;

    private String clientName = "default_client"; //TODO PROBABLY DO SMTH WITH IT

    @Override
    public void completed(AsynchronousSocketChannel channel, Object attachment) {
        clientListener.accept(null, this);
        ByteBuffer byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);

        clientName = readMessage(channel, byteBuffer);
        responseWithIdForClient(channel, String.format(FORMAT_FOR_ID, atomicId.getAndIncrement()), clientName);

        while (true) {
            String message = readMessage(channel, byteBuffer);
            if (isEmpty(message)) {
                break;
            }
            executorService.execute(() -> messageHandler.handle(channel, message));
        }
        closeConnection();
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        closeConnection();
    }

    private void responseWithIdForClient(AsynchronousSocketChannel channel, String currentId, String clientName) {
        log.info("{} connected, received ID: {} ", clientName, currentId);
        sendMessage(channel, currentId);
        updateAndPrintRoutingMap(clientName, channel);
    }

    private void closeConnection() {
        log.info("Closed connection with {}", clientName);
        routingMap.remove(clientName);
        printRoutingMap();
    }

    private void updateAndPrintRoutingMap(String clientName, AsynchronousSocketChannel channel) {
        routingMap.put(clientName, channel);
        printRoutingMap();
    }

    private void printRoutingMap() {
        System.out.println("Routing map: ");
        System.out.println(routingMap.keySet().toString());
    }
}
