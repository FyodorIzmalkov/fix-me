package com.lsandor.fixme.market.handler;

import com.lsandor.fixme.core.Core;
import com.lsandor.fixme.core.MessageType;
import com.lsandor.fixme.core.exception.NoFixTagException;
import com.lsandor.fixme.core.tags.FIX_tag;

import java.nio.channels.AsynchronousSocketChannel;

public class MarketTagsValidator extends MessageHandlerWithId {

    public MarketTagsValidator(String id, String name) {
        super(id, name);
    }

    @Override
    public void handle(AsynchronousSocketChannel channel, String message) {
        try {
            Core.getFixValueFromMessageByTag(message, FIX_tag.INSTRUMENT);
            final int price = Integer.parseInt(Core.getFixValueFromMessageByTag(message, FIX_tag.PRICE));
            final int quantity = Integer.parseInt(Core.getFixValueFromMessageByTag(message, FIX_tag.QUANTITY));
            if (quantity <= 0 || quantity > 10000) {
                rejectedMessage(channel, message, "Wrong quantity(1-10k)");
                return;
            } else if (price <= 0 || price > 10000) {
                rejectedMessage(channel, message, "Wrong price(1-10k");
                return;
            }

            final String type = Core.getFixValueFromMessageByTag(message, FIX_tag.TYPE);
            if (MessageType.is(type)) {
                super.handle(channel, message);
            } else {
                rejectedMessage(channel, message, "Wrong operation type");
            }
        } catch (NoFixTagException ex) {
            rejectedMessage(channel, message, "Wrong fix tags");
        } catch (NumberFormatException ex) {
            rejectedMessage(channel, message, "Wrong value type");
        }
    }
}
