package com.lsandor.fixme.market.handler;



import com.lsandor.fixme.core.Core;
import com.lsandor.fixme.core.tags.FIX_tag;
import com.lsandor.fixme.core.MessageType;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class MessageExecutor extends MessageHandlerWithId {

    private final Map<String, Integer> instruments;

    public MessageExecutor(String clientId, String name, Map<String, Integer> instruments) {
        super(clientId, name);
        this.instruments = instruments;
    }

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        final String instrument = Core.getFixValueByTag(message, FIX_tag.INSTRUMENT);
        if (instruments.containsKey(instrument)) {
            final int quantity = Integer.parseInt(Core.getFixValueByTag(message, FIX_tag.QUANTITY));
            final int marketQuantity = instruments.get(instrument);
            final String type = Core.getFixValueByTag(message, FIX_tag.TYPE);
            if (type.equals(MessageType.Buy.toString())) {
                if (marketQuantity < quantity) {
                    rejectedMessage(channel, message, "Not enough instruments");
                    return;
                } else {
                    instruments.put(instrument, marketQuantity - quantity);
                }
            } else {
                instruments.put(instrument, marketQuantity + quantity);
            }
            System.out.println("Market instruments: " + instruments.toString());
            executedMessage(channel, message, "OK");
        } else {
            rejectedMessage(channel, message, instrument + " instrument is not traded on the market");
        }
    }

    @Override
    protected boolean isInsertMessagesToDb() {
        return true;
    }
}
