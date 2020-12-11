package com.lsandor.fixme.broker;

import static com.lsandor.fixme.core.utils.Utils.getOrGenerateClientName;

public class Main {
    public static void main(String[] args) {
        Broker broker = new Broker(getOrGenerateClientName(args));
        broker.run();
    }
}
