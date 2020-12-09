package com.lsandor.fixme.market;

import static com.lsandor.fixme.core.utils.Utils.getOrGenerateClientName;

public class Main {
    public static void main(String[] args) {
        Market market = new Market(getOrGenerateClientName(args));
        market.run();
    }
}
