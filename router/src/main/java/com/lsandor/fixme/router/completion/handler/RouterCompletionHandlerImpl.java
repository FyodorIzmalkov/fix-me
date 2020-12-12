package com.lsandor.fixme.router.completion.handler;

import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.router.map.RouterMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lsandor.fixme.core.messenger.Messenger.readMessage;
import static com.lsandor.fixme.core.messenger.Messenger.sendMessage;
import static com.lsandor.fixme.core.utils.Constants.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@RequiredArgsConstructor
public class RouterCompletionHandlerImpl implements CompletionHandler<AsynchronousSocketChannel, Object> {

    private final ExecutorService executorService = Executors.newFixedThreadPool(EXECUTOR_THREADS);
    private final RouterMap routerMap;
    private final AsynchronousServerSocketChannel clientListener;
    private final AtomicInteger atomicId;
    private final MessageHandler messageHandler;
    private String clientId = ZERO_ID;

    @Override
    public void completed(AsynchronousSocketChannel channel, Object attachment) {
        clientListener.accept(null, this);
        ByteBuffer byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);

        String clientName = readMessage(channel, byteBuffer);
        clientId = String.format(FORMAT_FOR_ID, atomicId.getAndIncrement());
        sendResponseWithIdForClient(channel, clientId, clientName);
        updateAndPrintRoutingMap(clientId, clientName, channel);

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

    private void sendResponseWithIdForClient(AsynchronousSocketChannel channel, String currentId, String clientName) {
        log.info("{} connected, received ID: {} ", clientName, currentId);
        sendMessage(channel, currentId);
    }

    private void closeConnection() {
        log.info("Closed connection with {}", clientId);
        routerMap.removeChannelByClientId(clientId);
        printRoutingMap();
    }

    private void updateAndPrintRoutingMap(String clientId, String clientName, AsynchronousSocketChannel channel) {
        routerMap.putClientNameAndIdWithChannel(clientId, clientName, channel);
        printRoutingMap();
    }

    private void printRoutingMap() {
        System.out.println(routerMap.toString());
    }
}
