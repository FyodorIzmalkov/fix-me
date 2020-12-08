package com.lsandor.fixme.broker;


import com.lsandor.fixme.broker.handler.ExecutionResult;
import com.lsandor.fixme.broker.handler.ResultTagValidator;
import com.lsandor.fixme.core.client.Client;
import com.lsandor.fixme.core.Core;
import com.lsandor.fixme.core.messenger.Messenger;
import com.lsandor.fixme.core.utils.Utils;
import com.lsandor.fixme.core.exception.UserInputValidationException;
import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.utils.Constants;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Broker extends Client {

    public static final String NAME_PREFIX = "B";

    private Broker(String name) {
        super(Constants.BROKER_PORT, NAME_PREFIX + name);
    }

    private void start() {
        try {
            readFromSocket();

            final Scanner scanner = new Scanner(System.in);
            System.out.println("Message to send " + Constants.USER_MESSAGE_FORMAT + ":");
            while (true) {
                try {
                    final String message = Core.userInputToFixMessage(scanner.nextLine(), getId(), getName());
                    final Future<Integer> result = Messenger.sendMessage(getSocketChannel(), message);
                    result.get();
                } catch (UserInputValidationException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = super.getMessageHandler();
        final MessageHandler resultTag = new ResultTagValidator();
        final MessageHandler executionResult = new ExecutionResult();
        messageHandler.setNextHandler(resultTag);
        resultTag.setNextHandler(executionResult);
        return messageHandler;
    }

    public static void main(String[] args) {
        new Broker(Utils.getClientName(args)).start();
    }
}
