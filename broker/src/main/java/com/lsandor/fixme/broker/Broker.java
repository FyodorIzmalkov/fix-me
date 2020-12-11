package com.lsandor.fixme.broker;


import com.lsandor.fixme.broker.handler.StatusValidator;
import com.lsandor.fixme.broker.handler.TransactionResult;
import com.lsandor.fixme.core.client.Counterparty;
import com.lsandor.fixme.core.exception.UserInputValidationException;
import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.model.Instrument;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static com.lsandor.fixme.core.Core.userInputToFixMessage;
import static com.lsandor.fixme.core.messenger.Messenger.sendMessage;
import static com.lsandor.fixme.core.utils.Constants.*;

@Slf4j
public class Broker extends Counterparty {

    private final Scanner scanner = new Scanner(System.in);
    private final Map<Instrument, Long> instrumentMap = new ConcurrentHashMap<>();

    public Broker(String name) {
        super(BROKER_PORT, BROKER_NAME_PREFIX + name);
    }

    public void run() {
        log.info("Broker started, name: {}", this.getName());
        try {
            initConnectionWithRouter();
            readFromSocket(createCompletionHandler());

            log.info("Type message to send, example: {}", EXAMPLE_MESSAGE_FOR_BROKER);
            while (true) {
                try {
                    String fixMessageToSend = userInputToFixMessage(scanner.nextLine(), getId(), getName()); // TODO
                    sendMessage(getChannel(), fixMessageToSend).get();
                } catch (UserInputValidationException e) {
                    System.out.println(e.getMessage()); // TODO
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected MessageHandler createMessageHandler() {
        MessageHandler messageHandler = super.createMessageHandler();
        MessageHandler resultTag = new StatusValidator();
        MessageHandler executionResult = new TransactionResult(instrumentMap);

        messageHandler.setNextHandler(resultTag);
        resultTag.setNextHandler(executionResult);
        return messageHandler;
    }
}
