package com.lsandor.fixme.core.model;


import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.channels.AsynchronousSocketChannel;

@Getter
@Builder
@RequiredArgsConstructor
public class MessageToSend {
    private final String message;
    private final AsynchronousSocketChannel channel;
}
