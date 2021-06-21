package cc.i9mc.gamecore.queue.lobbyFactory;

import cc.i9mc.gamecore.queue.iFactory.IBalancer;
import cc.i9mc.gamecore.queue.iFactory.IQueue;
import cc.i9mc.gamecore.queue.iFactory.IServerACK;
import cc.i9mc.gamecore.queue.iFactory.IServerQueue;

/**
 * Created by JinVan on 2021-01-30.
 */
public class LobbyBalancer extends IBalancer {

    public LobbyBalancer(String type, String perfix) {
        super(type, perfix);
    }

    @Override
    public IServerACK getServerACK() {
        return new LobbyServerACK(this);
    }

    @Override
    public IQueue getQueue() {
        return new LobbyQueue(this);
    }

    @Override
    public IServerQueue getServerQueue() {
        return new LobbyServerQueue(this);
    }
}
