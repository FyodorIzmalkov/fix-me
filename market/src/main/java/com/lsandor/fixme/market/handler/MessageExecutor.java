package com.lsandor.fixme.market.handler;


import com.lsandor.fixme.core.messenger.MessageType;
import com.lsandor.fixme.core.model.Instrument;
import com.lsandor.fixme.core.tags.FIX_tag;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

import static com.lsandor.fixme.core.Core.getFixMapByTag;
import static com.lsandor.fixme.core.tags.FIX_tag.*;

@Slf4j
public class MessageExecutor extends BaseMessageHandler {

    private final Map<Instrument, Long> availableInstruments;

    public MessageExecutor(String clientId, String name, Map<Instrument, Long> instruments) {
        super(clientId, name);
        this.availableInstruments = instruments;
    }

    @Override
    public void handle(AsynchronousSocketChannel channel, String fixMessage) {
        Map<FIX_tag, String> messageMap = getFixMapByTag(fixMessage);
        Instrument instrument = Instrument.valueOf(messageMap.get(INSTRUMENT));
        if (availableInstruments.containsKey(instrument)) {
            long quantity = Long.parseLong(messageMap.get(QUANTITY));
            long availableQuantity = availableInstruments.get(instrument);

            MessageType messageType = MessageType.valueOf(messageMap.get(TYPE));
            if (MessageType.BUY == messageType) {
                if (availableQuantity < quantity) {
                    responseWithStatusRejected(channel, fixMessage, "Not enough quantity of instrument: " + instrument.name());
                    return;
                } else {
                    log.info("We have a BUY transaction for " + instrument.name() + " quantity: " + quantity);
                    availableInstruments.put(instrument, availableQuantity - quantity);
                }
            } else {
                log.info("We have a SELL transaction for " + instrument.name() + " quantity: " + quantity);
                availableInstruments.put(instrument, availableQuantity + quantity);
            }
            log.info("Market instruments: " + availableInstruments.toString());
            responseWithStatusExecuted(channel, fixMessage, "Transaction successfully completed");
        } else {
            responseWithStatusRejected(channel, fixMessage, instrument + " there is no such instrument on the market");
        }
    }

    @Override
    protected boolean saveTransactionToDatabase() {
        return true;
    }
}
