package com.lsandor.fixme.market.handler;

import com.lsandor.fixme.core.messenger.MessageType;
import com.lsandor.fixme.core.model.Instrument;
import com.lsandor.fixme.core.tags.FIX_tag;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

import static com.lsandor.fixme.core.Core.getFixMapByTag;
import static com.lsandor.fixme.core.tags.FIX_tag.*;
import static org.apache.commons.lang3.EnumUtils.isValidEnum;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MarketMandatoryTagsValidator extends BaseMessageHandler {

    public MarketMandatoryTagsValidator(String id, String name) {
        super(id, name);
    }

    @Override
    public void handle(AsynchronousSocketChannel channel, String fixMessage) {
        if (messageIsValidated(channel, fixMessage)) {
            super.handle(channel, fixMessage);
        }
    }

    private boolean messageIsValidated(AsynchronousSocketChannel channel, String fixMessage) {
        boolean isMessageValid = true;
        Map<FIX_tag, String> resultMap = getFixMapByTag(fixMessage);

        String instrument = resultMap.get(INSTRUMENT);
        if (isEmpty(instrument)) {
            responseWithStatusRejected(channel, fixMessage, "Instrument is a mandatory field to fill");
            isMessageValid = false;
        }

        if (!isValidEnum(Instrument.class, instrument)) {
            responseWithStatusRejected(channel, fixMessage, "There is no such instrument type: " + instrument);
            isMessageValid = false;
        }

        String marketNameStr = resultMap.get(TARGET_NAME);
        if (isEmpty(marketNameStr) || !getName().equals(marketNameStr)) {
            responseWithStatusRejected(channel, fixMessage, "Market name is wrong or missing: " + marketNameStr);
            isMessageValid = false;
        }

        String priceStr = resultMap.get(PRICE);
        if (isEmpty(priceStr)) {
            responseWithStatusRejected(channel, fixMessage, "Price is a mandatory field to fill");
            isMessageValid = false;
        }

        String quantityStr = resultMap.get(QUANTITY);
        if (isEmpty(quantityStr)) {
            responseWithStatusRejected(channel, fixMessage, "Quantity is a mandatory field to fill");
            isMessageValid = false;
        }

        String messageTypeStr = resultMap.get(TYPE);
        if (isEmpty(messageTypeStr)) {
            responseWithStatusRejected(channel, fixMessage, "MessageType is a mandatory field to fill");
            isMessageValid = false;
        }

        if (!isValidEnum(MessageType.class, messageTypeStr)) {
            responseWithStatusRejected(channel, fixMessage, "Bad message type");
            isMessageValid = false;
        }

        try {
            int price = Integer.parseInt(priceStr);
            if (price <= 0) {
                responseWithStatusRejected(channel, fixMessage, "Price must be more than 0");
                isMessageValid = false;
            }

            long quantity = Long.parseLong(quantityStr);
            if (quantity <= 0) {
                responseWithStatusRejected(channel, fixMessage, "Quantity must be more than 0");
                isMessageValid = false;
            }
        } catch (NumberFormatException e) {
            responseWithStatusRejected(channel, fixMessage, "Wrong integer value format");
            return false;
        }
        return isMessageValid;
    }
}
