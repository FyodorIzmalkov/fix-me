package com.lsandor.fixme.core.client;


import com.lsandor.fixme.core.completion.handler.CommonCompletionHandler;
import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.messenger.Messenger;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.lsandor.fixme.core.utils.Constants.*;
import static com.lsandor.fixme.core.utils.Utils.createCommonMessageHandler;

@Slf4j
public abstract class Counterparty {

    protected final ByteBuffer byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    private final int port;
    @Getter
    private final String name;

    @Setter
    private AsynchronousSocketChannel channel = null;
    @Getter
    private String id;

    public Counterparty(int port, String name) {
        this.port = port;
        this.name = name;
        this.id = ZERO_ID;
    }

    protected AsynchronousSocketChannel getChannel() {
        if (this.channel == null || !this.channel.isOpen()) {
            this.channel = tryToConnectToRouter();
            establishConnectionAndReceiveId();
            return channel;
        }
        return channel;
    }

    @NotNull
    private AsynchronousSocketChannel tryToConnectToRouter() {
        while (true) {
            AsynchronousSocketChannel channel = null;
            try {
                channel = AsynchronousSocketChannel.open();
                InetSocketAddress routerAddress = new InetSocketAddress(LOCALHOST, port);
                Future<Void> future = channel.connect(routerAddress);
                future.get(10L, TimeUnit.SECONDS);
            } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
                log.error("{} could not connect to Router, trying to reconnect.", this.name);
            }
            if (channel != null && channel.isOpen()) {
                return channel;
            }

            try {
                Thread.sleep(5000L);
            } catch (InterruptedException ex) {
                log.error(ex.getLocalizedMessage());
            }
        }
    }

    private void establishConnectionAndReceiveId() {
        Messenger.sendMessage(this.channel, this.name);
        this.id = Messenger.readMessage(this.channel, this.byteBuffer);
        log.info("{} received id: {}", this.name, this.id);
    }

    protected void readFromSocket(CompletionHandler<Integer, Object> completionHandler) {
        getChannel().read(byteBuffer, null, completionHandler);
    }

    protected void initConnectionWithRouter() {
        if (this.channel == null) {
            this.channel = tryToConnectToRouter();
            establishConnectionAndReceiveId();
        }
    }

    protected CommonCompletionHandler createCompletionHandler() {
        return new CommonCompletionHandler(
                this.byteBuffer,
                createMessageHandler(),
                this::getChannel,
                this::setChannel
        );
    }

    protected MessageHandler createMessageHandler() {
        return createCommonMessageHandler();
    }
}
