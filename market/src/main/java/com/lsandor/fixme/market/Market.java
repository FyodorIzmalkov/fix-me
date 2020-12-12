package com.lsandor.fixme.market;


import com.lsandor.fixme.core.client.Counterparty;
import com.lsandor.fixme.core.handler.MessageHandler;
import com.lsandor.fixme.core.handler.impl.MandatoryTagsValidator;
import com.lsandor.fixme.core.handler.impl.MessageChecksumValidator;
import com.lsandor.fixme.core.handler.impl.SystemMessageHandler;
import com.lsandor.fixme.core.model.Instrument;
import com.lsandor.fixme.market.handler.MarketMandatoryTagsValidator;
import com.lsandor.fixme.market.handler.MessageExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.lsandor.fixme.core.utils.Constants.MARKET_NAME_PREFIX;
import static com.lsandor.fixme.core.utils.Constants.MARKET_PORT;
import static com.lsandor.fixme.core.utils.Utils.getRandomInstrumentsForTheMarket;

@Slf4j
public class Market extends Counterparty {

    private final Map<Instrument, Long> instruments;

    public Market(String name) {
        super(MARKET_PORT, MARKET_NAME_PREFIX.concat(name));
        this.instruments = getRandomInstrumentsForTheMarket();
    }

    @Override
    public void run() {
        log.info("Market started, available instruments: {}, market name: {}", instruments.toString(), this.getName());
        super.run();

        while (true) {
            try {
                Thread.sleep(10_000L);
            } catch (InterruptedException ex) {
                log.error(ex.getLocalizedMessage());
            }
        }
    }

    @Override
    protected MessageHandler createMessageHandler() {
        MessageHandler messageHandler = new SystemMessageHandler();
        MessageHandler mandatoryTagsValidator = new MandatoryTagsValidator();
        MessageHandler checksumValidator = new MessageChecksumValidator();
        MessageHandler marketTagsValidator = new MarketMandatoryTagsValidator(getId(), this.getName()); //TODO!!!
        MessageHandler messageExecutor = new MessageExecutor(this.getId(), this.getName(), instruments);

        messageHandler.setNextHandler(mandatoryTagsValidator);
        mandatoryTagsValidator.setNextHandler(checksumValidator);
        checksumValidator.setNextHandler(marketTagsValidator);
        marketTagsValidator.setNextHandler(messageExecutor);
        return messageHandler;
    }
}
