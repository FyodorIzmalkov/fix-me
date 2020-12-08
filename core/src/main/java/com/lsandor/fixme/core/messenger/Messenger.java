package com.lsandor.fixme.core.messenger;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.lsandor.fixme.core.utils.Constants.EMPTY_STRING;
import static com.lsandor.fixme.core.utils.Constants.SYSTEM_MESSAGE;

@Slf4j
public class Messenger {

    public static String readMessage(AsynchronousSocketChannel channel, ByteBuffer readBuffer) {
        try {
            return read(channel.read(readBuffer).get(), readBuffer);
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getLocalizedMessage());
            return EMPTY_STRING;
        }
    }

    public static String read(int bytesRead, ByteBuffer readBuffer) {
        if (bytesRead != -1) {
            readBuffer.flip();
            byte[] bytesArr = new byte[bytesRead];
            readBuffer.get(bytesArr);
            readBuffer.clear();
            String message = new String(bytesArr);
            log.info("Read message: {}", message);

            return message;
        }
        return EMPTY_STRING;
    }

    public static Future<Integer> sendMessage(AsynchronousSocketChannel channel, String message) {
        log.info("Sent: {}", message);
        return channel.write(ByteBuffer.wrap(message.getBytes()));
    }

    public static Future<Integer> sendSystemMessage(AsynchronousSocketChannel channel, String message) {
        log.info("Sent system message: {}", message);
        return channel.write(ByteBuffer.wrap(SYSTEM_MESSAGE.concat(message).getBytes()));
    }
}
