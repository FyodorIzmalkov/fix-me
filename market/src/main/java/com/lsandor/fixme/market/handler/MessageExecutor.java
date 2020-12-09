package com.lsandor.fixme.market.handler;


import com.lsandor.fixme.core.Core;
import com.lsandor.fixme.core.messenger.MessageType;
import com.lsandor.fixme.core.model.Instrument;
import com.lsandor.fixme.core.tags.FIX_tag;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class MessageExecutor extends BaseMessageHandler {

    private final Map<Instrument, Long> instruments;

    public MessageExecutor(String clientId, String name, Map<Instrument, Long> instruments) {
        super(clientId, name);
        this.instruments = instruments;
    }

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        final String instrument = Core.getFixValueFromMessageByTag(message, FIX_tag.INSTRUMENT);
        if (instruments.containsKey(instrument)) {
            final int quantity = Integer.parseInt(Core.getFixValueFromMessageByTag(message, FIX_tag.QUANTITY));
            final long marketQuantity = instruments.get(instrument);
            final String type = Core.getFixValueFromMessageByTag(message, FIX_tag.TYPE);
            if (type.equals(MessageType.BUY.toString())) {
                if (marketQuantity < quantity) {
                    responseWithStatusRejected(channel, message, "Not enough instruments");
                    return;
                } else {
                    instruments.put(instrument, marketQuantity - quantity);
                }
            } else {
                instruments.put(instrument, marketQuantity + quantity);
            }
            System.out.println("Market instruments: " + instruments.toString());
            responseWithStatusExecuted(channel, message, "OK");
        } else {
            responseWithStatusRejected(channel, message, instrument + " instrument is not traded on the market");
        }
    }

    @Override
    protected boolean isInsertMessagesToDb() {
        return true;
    }
}
