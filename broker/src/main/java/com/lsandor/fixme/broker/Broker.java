package com.lsandor.fixme.broker;


import com.lsandor.fixme.broker.handler.StatusValidator;
import com.lsandor.fixme.core.client.Counterparty;
import com.lsandor.fixme.core.exception.BadUserInputException;
import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.handler.impl.MandatoryTagsValidator;
import com.lsandor.fixme.core.handler.impl.MessageChecksumValidator;
import com.lsandor.fixme.core.handler.impl.SystemMessageHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import static com.lsandor.fixme.core.Core.addPart;
import static com.lsandor.fixme.core.Core.calculateChecksumFromString;
import static com.lsandor.fixme.core.messenger.Messenger.sendMessage;
import static com.lsandor.fixme.core.tags.FIX_tag.*;
import static com.lsandor.fixme.core.utils.Constants.*;
import static org.apache.commons.lang3.StringUtils.SPACE;

@Slf4j
public class Broker extends Counterparty {

    private final Scanner scanner = new Scanner(System.in);

    public Broker(String name) {
        super(BROKER_PORT, BROKER_NAME_PREFIX + name);
    }

    @Override
    public void run() {
        log.info("Broker started, name: {}", this.getName());
        try {
            super.run();

            log.info("Type a message to send, example: {}", EXAMPLE_MESSAGE_FOR_BROKER);
            while (true) {
                try {
                    String userInputFixMessage = createFixMessageFromUserInput(scanner.nextLine(), getId(), getName());
                    sendMessage(getChannel(), userInputFixMessage).get();
                } catch (BadUserInputException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected MessageHandler createMessageHandler() {
        MessageHandler messageHandler = new SystemMessageHandler();
        MessageHandler mandatoryTagsValidator = new MandatoryTagsValidator();
        MessageHandler checksumValidator = new MessageChecksumValidator();
        MessageHandler resultTag = new StatusValidator();

        messageHandler.setNextHandler(mandatoryTagsValidator);
        mandatoryTagsValidator.setNextHandler(checksumValidator);
        checksumValidator.setNextHandler(resultTag);
        return messageHandler;
    }

    private String createFixMessageFromUserInput(String userInputString, String brokerId, String brokerName) throws BadUserInputException {
        String[] inputArray = userInputString.split(SPACE);
        if (inputArray.length != 6) {
            throw new BadUserInputException("Bad input, input should be formatted this way: " + USER_INPUT_FORMAT);
        }

        StringBuilder resultBuilder = new StringBuilder();
        addPart(resultBuilder, SOURCE_ID, brokerId);
        addPart(resultBuilder, SOURCE_NAME, brokerName);
        addPart(resultBuilder, TARGET_ID, inputArray[TARGET_ID_NUM]);
        addPart(resultBuilder, TARGET_NAME, inputArray[TARGET_NAME_NUM]);
        addPart(resultBuilder, TYPE, inputArray[TYPE_NUM]);
        addPart(resultBuilder, INSTRUMENT, inputArray[INSTRUMENT_NUM]);
        addPart(resultBuilder, QUANTITY, inputArray[QUANTITY_NUM]);
        addPart(resultBuilder, PRICE, inputArray[PRICE_NUM]);
        addPart(resultBuilder, CHECKSUM, calculateChecksumFromString(resultBuilder.toString()));

        return resultBuilder.toString();
    }
}
