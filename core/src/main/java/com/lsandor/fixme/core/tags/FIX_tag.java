package com.lsandor.fixme.core.tags;

public enum FIX_tag {

    SOURCE_ID(0),
    SOURCE_NAME(1),
    TARGET_ID(2),
    TARGET_NAME(3),
    INSTRUMENT(4),
    QUANTITY(5),
    PRICE(6),
    TYPE(7),
    STATUS(8),
    MESSAGE(9),
    CHECKSUM(10);

    private final int num;

    FIX_tag(int num) {
        this.num = num;
    }

    public int getValue() {
        return num;
    }
}
