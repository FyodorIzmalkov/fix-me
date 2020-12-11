package com.lsandor.fixme.core.completion.handler;

import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.messenger.Messenger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
@RequiredArgsConstructor
public class CommonCompletionHandler implements CompletionHandler<Integer, Object> {

    private final ByteBuffer byteBuffer;
    private final MessageHandler messageHandler;
    private final Supplier<AsynchronousSocketChannel> channelSupplier;
    private final Consumer<AsynchronousSocketChannel> channelSetter;

    @Override
    public void completed(Integer result, Object attachment) {
        String receivedMessage = Messenger.read(result, byteBuffer);
        log.info("RECEIVED MESSAGE: {}", receivedMessage);
        if (isNotEmpty(receivedMessage)) {
            messageHandler.handle(channelSupplier.get(), receivedMessage);
            channelSupplier.get().read(byteBuffer, null, this);
        } else {
            tryToReconnect();
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        tryToReconnect();
    }

    private void tryToReconnect() {
        log.error("We have problems with connection, trying to reconnect.");
        channelSetter.accept(null);
        channelSupplier.get().read(byteBuffer, null, this);
    }
}
