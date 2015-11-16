package ru.spbau.mit;

import java.util.*;


public class SumTwoNumbersGame implements Game {
    private Integer X;
    private Integer Y;
    private final GameServer server;

    public SumTwoNumbersGame(GameServer server) {
        this.server = server;
        restart();
    }

    private void restart() {
        Random random = new Random();
        X = Math.abs(random.nextInt());
        Y = Math.abs(random.nextInt());
    }

    private String getGameData() {
        return X.toString() + " " + Y.toString();
    }

    @Override
    public void onPlayerConnected(String id) {
        server.sendTo(id, getGameData());
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        Integer sum = Integer.parseInt(msg);
        if (sum == X + Y) {
            server.sendTo(id, "Right");
            server.broadcast(id + " won");
            restart();
            server.broadcast(getGameData());
        } else {
            server.sendTo(id, "Wrong");
        }
    }
}
