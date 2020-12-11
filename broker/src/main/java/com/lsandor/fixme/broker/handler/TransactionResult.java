package com.lsandor.fixme.broker.handler;


import com.lsandor.fixme.core.handler.AbstractMessageHandler;
import com.lsandor.fixme.core.messenger.MessageType;
import com.lsandor.fixme.core.model.Instrument;
import com.lsandor.fixme.core.status.Status;
import com.lsandor.fixme.core.tags.FIX_tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

import static com.lsandor.fixme.core.Core.getFixMapByTag;
import static com.lsandor.fixme.core.tags.FIX_tag.*;
import static org.apache.commons.lang3.EnumUtils.isValidEnum;

@Slf4j
@RequiredArgsConstructor
public class TransactionResult extends AbstractMessageHandler {

    private final Map<Instrument, Long> instrumentMap;

    @Override
    public void handle(AsynchronousSocketChannel channel, String fixMessage) {
        Map<FIX_tag, String> resultMap = getFixMapByTag(fixMessage);
        String status = resultMap.get(STATUS);
        String message = resultMap.get(MESSAGE);
        String messageType = resultMap.get(TYPE);
        log.info("Transaction completed with result: {} and message: {}", status, message);

        if (Status.EXECUTED == Status.valueOf(status) && MessageType.BUY == MessageType.valueOf(messageType)) {
            String instrumentStr = resultMap.get(INSTRUMENT);
            if (isValidEnum(Instrument.class, instrumentStr)) {
                Instrument instrument = Instrument.valueOf(instrumentStr);
                Long quantity = Long.parseLong(resultMap.get(QUANTITY));
                instrumentMap.computeIfPresent(instrument, (k, v) -> v + quantity);
                instrumentMap.putIfAbsent(instrument, quantity);
                log.info("Instruments bought: {}", instrumentMap.toString());
            }
        }
        super.handle(channel, fixMessage);
    }
}
