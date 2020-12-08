package com.lsandor.fixme.core.messenger;

import com.lsandor.fixme.core.utils.Constants;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public class Messenger {

    public static String readMessage(AsynchronousSocketChannel channel, ByteBuffer readBuffer) {
        try {
            return read(channel.read(readBuffer).get(), readBuffer);
        } catch (InterruptedException | ExecutionException e) {
            return Constants.EMPTY_MESSAGE;
        }
    }

    public static String read(int bytesRead, ByteBuffer readBuffer) {
        if (bytesRead != -1) {
            readBuffer.flip();
            byte[] bytes = new byte[bytesRead];
            readBuffer.get(bytes, 0, bytesRead);
            readBuffer.clear();
            String message = new String(bytes);
            System.out.println("Got: " + message);
            return message;
        }
        return Constants.EMPTY_MESSAGE;
    }

    public static Future<Integer> sendMessage(AsynchronousSocketChannel channel, String message) {
        log.info("Sent: {}", message);
        return channel.write(ByteBuffer.wrap(message.getBytes()));
    }

    public static Future<Integer> sendInternalMessage(AsynchronousSocketChannel channel, String message) {
        System.out.println("Send internal: " + message);
        final String internalMessage = Constants.INTERNAL_MESSAGE + message;
        return channel.write(ByteBuffer.wrap(internalMessage.getBytes()));
    }
}
