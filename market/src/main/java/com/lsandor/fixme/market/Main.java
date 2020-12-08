package com.lsandor.fixme.market;

import com.lsandor.fixme.core.utils.Utils;

public class Main {
    public static void main(String[] args) {
        new Market(Utils.getClientName(args)).start();
    }
}
