package com.lsandor.fixme.core.tags;

public enum FIX_tag {

    ID(0),
    SOURCE_NAME(1),
    TARGET_NAME(2),
    INSTRUMENT(3),
    QUANTITY(4),
    PRICE(5),
    TYPE(6),
    RESULT(8),
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
