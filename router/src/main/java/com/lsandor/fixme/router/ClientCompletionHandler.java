package com.lsandor.fixme.router;

import com.lsandor.fixme.core.messenger.Messenger;
import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.utils.Constants;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

class ClientCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

    private static final int EXECUTOR_THREADS = 5;

    private final ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_THREADS);
    private final AsynchronousServerSocketChannel clientListener;
    private final Map<String, AsynchronousSocketChannel> routingTable;
    private final AtomicInteger id;
    private final MessageHandler messageHandler;

    private String clientName = "client";

    ClientCompletionHandler(AsynchronousServerSocketChannel clientListener, Map<String, AsynchronousSocketChannel> routingTable,
                            AtomicInteger id, MessageHandler messageHandler) {
        this.clientListener = clientListener;
        this.routingTable = routingTable;
        this.id = id;
        this.messageHandler = messageHandler;
    }

    @Override
    public void completed(AsynchronousSocketChannel channel, Object attachment) {
        clientListener.accept(null, this);
        final ByteBuffer buffer = ByteBuffer.allocate(Constants.DEFAULT_BUFFER_SIZE);
        clientName = Messenger.readMessage(channel, buffer);

        sendClientId(channel, getNextId());

        while (true) {
            final String message = Messenger.readMessage(channel, buffer);
            if (Constants.EMPTY_MESSAGE.equals(message)) {
                break;
            }
            executor.execute(() -> messageHandler.handle(channel, message));
        }
        endConnection();
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        endConnection();
    }

    private void sendClientId(AsynchronousSocketChannel channel, String currentId) {
        System.out.println();
        System.out.println(clientName + " " + clientName + " connected, ID: " + currentId);
        Messenger.sendMessage(channel, currentId);
        routingTable.put(clientName, channel);
        printRoutingTable();
    }

    private void endConnection() {
        routingTable.remove(clientName);
        System.out.println();
        System.out.println(clientName + " " + clientName + " connection ended, Bye");
        printRoutingTable();
    }

    private void printRoutingTable() {
        System.out.println("Routing table: " + routingTable.keySet().toString());
    }

    private String getNextId() {
        return String.format(Constants.ID_FORMAT, id.getAndIncrement());
    }
}
