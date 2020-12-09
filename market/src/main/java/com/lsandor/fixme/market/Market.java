package com.lsandor.fixme.market;


import com.lsandor.fixme.core.client.Counterparty;
import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.model.Instrument;
import com.lsandor.fixme.market.completion.handler.MarketCompletionHandlerImpl;
import com.lsandor.fixme.market.handler.MarketMandatoryTagsValidator;
import com.lsandor.fixme.market.handler.MessageExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.lsandor.fixme.core.utils.Constants.MARKET_NAME_PREFIX;
import static com.lsandor.fixme.core.utils.Constants.MARKET_PORT;
import static com.lsandor.fixme.core.utils.Utils.createCommonMessageHandler;
import static com.lsandor.fixme.core.utils.Utils.getRandomInstrumentsForTheMarket;

@Slf4j
public class Market extends Counterparty {

    private final Map<Instrument, Long> instruments;

    public Market(String name) {
        super(MARKET_PORT, MARKET_NAME_PREFIX.concat(name));
        this.instruments = getRandomInstrumentsForTheMarket();
    }

    public void run() {
        log.info("Market instruments: {}, name: {}", instruments.toString(), this.getName());
        readFromSocket(createMarketCompletionHandler());

        while (true) { // Notice that the server will no longer exit after a connection has been established unless we explicitly close it.
            // работаем пока не вырубят
        }
    }

    private MarketCompletionHandlerImpl createMarketCompletionHandler() {
        return new MarketCompletionHandlerImpl(
                this.byteBuffer,
                createMarketMessageHandler(),
                this::getChannel,
                this::setChannel
        );
    }

    private MessageHandler createMarketMessageHandler() {
        final MessageHandler messageHandler = createCommonMessageHandler();
        final MessageHandler marketTagsValidator = new MarketMandatoryTagsValidator(getId(), this.getName()); //TODO!!!
        final MessageHandler messageExecutor = new MessageExecutor(this.getId(), this.getName(), instruments);

        messageHandler.setNextHandler(marketTagsValidator);
        marketTagsValidator.setNextHandler(messageExecutor);
        return messageHandler;
    }
}
