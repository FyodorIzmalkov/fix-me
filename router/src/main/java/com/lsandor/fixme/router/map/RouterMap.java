package com.lsandor.fixme.router.map;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RouterMap {

    private final Map<String, AsynchronousSocketChannel> routingMapById = new ConcurrentHashMap<>();
    private final Map<String, AsynchronousSocketChannel> routingMapByName = new ConcurrentHashMap<>();

    @Nullable
    public AsynchronousSocketChannel tryToGetChannel(String string) {
        AsynchronousSocketChannel channel = routingMapById.get(string);

        if (channel != null && channel.isOpen()) {
            return channel;
        }

        return routingMapByName.get(string);
    }

    @Nullable
    public AsynchronousSocketChannel tryToGetChannelByNameOrId(String clientName, String clientId) {
        AsynchronousSocketChannel channel = routingMapById.get(clientId);

        if (channel != null) {
            return channel;
        }

        return routingMapByName.get(clientName);
    }

    public void putClientIdAndChannel(String clientId, AsynchronousSocketChannel channel) {
        routingMapById.put(clientId, channel);
    }

    public void putClientNameAndChannel(String clientName, AsynchronousSocketChannel channel) {
        routingMapByName.put(clientName, channel);
    }

    public void putClientNameAndIdWithChannel(String clientId, String clientName, AsynchronousSocketChannel channel) {
        routingMapById.put(clientId, channel);
        routingMapByName.put(clientName, channel);
    }

    public void removeChannelByClientId(String clientId) {
        try {
            routingMapById.remove(clientId).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "RouterMap{" +
                "routingMapById=" + routingMapById.keySet().toString() +
                ", routingMapByName=" + routingMapByName.keySet().toString() +
                '}';
    }
}
