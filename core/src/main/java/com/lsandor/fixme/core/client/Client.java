package com.lsandor.fixme.core.client;


import com.lsandor.fixme.core.messenger.Messenger;
import com.lsandor.fixme.core.handler.impl.ChecksumValidator;
import com.lsandor.fixme.core.handler.impl.InternalMessageHandler;
import com.lsandor.fixme.core.handler.impl.MandatoryTagsValidator;
import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.utils.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class Client {

    private static final String FAKE_ID = "000000";

    private final ByteBuffer buffer = ByteBuffer.allocate(Constants.DEFAULT_BUFFER_SIZE);
    private final int port;
    private final String name;

    private AsynchronousSocketChannel socketChannel;
    private String id = FAKE_ID;

    public Client(int port, String name) {
        this.port = port;
        this.name = name;
    }

    protected AsynchronousSocketChannel getSocketChannel() {
        if (socketChannel == null) {
            socketChannel = connectToMessageRouter();
            Messenger.sendMessage(socketChannel, name);
            id = Messenger.readMessage(socketChannel, buffer);
            System.out.println(name + " ID: " + id);
            return socketChannel;
        }
        return socketChannel;
    }

    private AsynchronousSocketChannel connectToMessageRouter() {
        final AsynchronousSocketChannel socketChannel;
        try {
            socketChannel = AsynchronousSocketChannel.open();
            final Future future = socketChannel.connect(new InetSocketAddress(Constants.LOCALHOST, port));
            future.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.out.println("Could not connect to Message Router, reconnecting...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            return connectToMessageRouter();
        }
        return socketChannel;
    }

    private void invalidateConnection() {
        socketChannel = null;
    }

    protected String getId() {
        return id;
    }

    protected String getName() {
        return name;
    }

    protected void readFromSocket() {
        getSocketChannel().read(buffer, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                final String message = Messenger.read(result, buffer);
                if (!Constants.EMPTY_MESSAGE.equals(message)) {
                    onSuccessRead(message);
                    getSocketChannel().read(buffer, null, this);
                } else {
                    reconnect();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                reconnect();
            }

            private void reconnect() {
                System.out.println("Message router died! Have to reconnect");
                invalidateConnection();
                getSocketChannel().read(buffer, null, this);
            }
        });
    }

    private void onSuccessRead(String message) {
        getMessageHandler().handle(getSocketChannel(), message);
    }

    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = new InternalMessageHandler();
        final MessageHandler mandatoryTagsValidator = new MandatoryTagsValidator();
        final MessageHandler checksumValidator = new ChecksumValidator();
        messageHandler.setNextHandler(mandatoryTagsValidator);
        mandatoryTagsValidator.setNextHandler(checksumValidator);
        return messageHandler;
    }
}
