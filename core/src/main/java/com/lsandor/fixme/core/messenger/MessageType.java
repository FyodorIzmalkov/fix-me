package com.lsandor.fixme.core.messenger;

public enum MessageType {
    BUY,
    SELL;

    public static boolean is(String type) {
        return type.equals(BUY.toString()) || type.equals(SELL.toString());
    }
}
