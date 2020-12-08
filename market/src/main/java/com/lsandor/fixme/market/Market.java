package com.lsandor.fixme.market;



import com.lsandor.fixme.core.client.Client;
import com.lsandor.fixme.core.utils.Utils;
import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.utils.Constants;
import com.lsandor.fixme.market.handler.MarketTagsValidator;
import com.lsandor.fixme.market.handler.MessageExecutor;

import java.util.Map;

public class Market extends Client {

    public static final String NAME_PREFIX = "M";
    private final Map<String, Integer> instruments;

    private Market(String name) {
        super(Constants.MARKET_PORT, NAME_PREFIX + name);
        instruments = Utils.getRandomInstruments();
    }

    private void start() {
        System.out.println("Market instruments: " + instruments.toString());
        readFromSocket();

        while (true) ;
    }

    @Override
    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = super.getMessageHandler();
        final MessageHandler tagsValidator = new MarketTagsValidator(getId(), getName());
        final MessageHandler messageExecutor = new MessageExecutor(getId(), getName(), instruments);
        messageHandler.setNextHandler(tagsValidator);
        tagsValidator.setNextHandler(messageExecutor);
        return messageHandler;
    }

    public static void main(String[] args) {
        new Market(Utils.getClientName(args)).start();
    }
}
