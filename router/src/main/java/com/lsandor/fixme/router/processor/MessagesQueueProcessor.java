package com.lsandor.fixme.router.processor;

import com.lsandor.fixme.core.model.MessageToSend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.lsandor.fixme.core.messenger.Messenger.sendMessage;
import static com.lsandor.fixme.core.utils.Constants.EXECUTOR_THREADS;

@Slf4j
@RequiredArgsConstructor
public class MessagesQueueProcessor {
    private final BlockingQueue<MessageToSend> messagesQueue;
    private final ExecutorService executorService = Executors.newFixedThreadPool(EXECUTOR_THREADS);

    public void startSendingMessages() {
        log.info("Queue processor started.");
        executorService.submit(this::doJob);
    }

    private void doJob() {
        try {
            MessageToSend messageToSend = messagesQueue.take();
            sendMessage(messageToSend.getChannel(), messageToSend.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
