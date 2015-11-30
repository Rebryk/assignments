package ru.spbau.mit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class GameServerImpl implements GameServer {
    private final Game game;
    private HashMap<String, Connection> connections;
    private Integer lastId;

    public GameServerImpl(String gameClassName, Properties properties)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> pluginClass = Class.forName(gameClassName);
        Object plugin = pluginClass.getConstructor(GameServer.class).newInstance(this);

        for (String key: properties.stringPropertyNames()) {
            String stringValue = properties.getProperty(key);
            String setterName = getSetterName(key);
            try {
                int intValue = Integer.parseInt(stringValue);
                Method setter = pluginClass.getMethod(setterName, Integer.class);
                setter.invoke(plugin, intValue);
            } catch (NumberFormatException e) {
                Method setter = pluginClass.getMethod(setterName, String.class);
                setter.invoke(plugin, stringValue);
            }
        }

        if (!(plugin instanceof Game)) {
            throw new IllegalArgumentException(gameClassName + " isn't instance of Game");
        }

        game = (Game) plugin;
        connections = new HashMap<String, Connection>();
        lastId = 0;
    }

    private String getSetterName(String name) {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    @Override
    public void accept(final Connection connection) {
        synchronized (connections) {
            connections.put(lastId.toString(), connection);

            connection.send(lastId.toString());

            Thread connectionThread = new Thread(new ServerRunnable(lastId.toString(), connection));
            connectionThread.start();
            ++lastId;
        }
    }

    @Override
    public void broadcast(String message) {
        synchronized (connections) {
            for (Connection connection: connections.values()) {
                connection.send(message);
            }
        }
    }

    @Override
    public void sendTo(String id, String message) {
        synchronized (connections) {
            connections.get(id).send(message);
        }
    }

    private class ServerRunnable implements Runnable {
        private final String id;
        private final Connection connection;
        private final int timeout = 1000;

        public ServerRunnable(String id, Connection connection) {
            this.id = id;
            this.connection = connection;
        }

        @Override
        public void run() {
            game.onPlayerConnected(id);
            while (!connection.isClosed()) {
                try {
                    synchronized (connection) {
                        if (!connection.isClosed()) {
                            String message = connection.receive(timeout);
                            if (message != null) {
                                game.onPlayerSentMsg(id, message);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }

            synchronized (connections) {
                connections.remove(id);
            }
        }
    }
}
