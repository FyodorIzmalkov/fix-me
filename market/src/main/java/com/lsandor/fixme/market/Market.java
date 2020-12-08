package com.lsandor.fixme.market;


import com.lsandor.fixme.core.client.Client;
import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.utils.Utils;
import com.lsandor.fixme.market.handler.MarketTagsValidator;
import com.lsandor.fixme.market.handler.MessageExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.lsandor.fixme.core.utils.Constants.MARKET_NAME_PREFIX;
import static com.lsandor.fixme.core.utils.Constants.MARKET_PORT;

@Slf4j
public class Market extends Client {

    private final Map<String, Integer> products;

    public Market(String name) {
        super(MARKET_PORT, MARKET_NAME_PREFIX + name);
        products = Utils.getRandomInstruments();
    }

    public void start() {
        log.info("Market products: {}", products.toString());
        readFromSocket();

        while (true) ;
    }

    @Override
    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = super.getMessageHandler();
        final MessageHandler tagsValidator = new MarketTagsValidator(getId(), getName());
        final MessageHandler messageExecutor = new MessageExecutor(getId(), getName(), products);

        messageHandler.setNextHandler(tagsValidator);
        tagsValidator.setNextHandler(messageExecutor);
        return messageHandler;
    }
}
