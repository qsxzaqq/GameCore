package cc.i9mc.gamecore.queue.gameFactory;

import cc.i9mc.gamecore.queue.iFactory.IBalancer;
import cc.i9mc.gamecore.queue.iFactory.IQueue;
import cc.i9mc.gamecore.queue.iFactory.IServerACK;
import cc.i9mc.gamecore.queue.iFactory.IServerQueue;

/**
 * Created by JinVan on 2021-01-30.
 */
public class GameBalancer extends IBalancer {

    public GameBalancer(String type, String perfix) {
        super(type, perfix);
    }

    @Override
    public IServerACK getServerACK() {
        return new GameServerACK(this);
    }

    @Override
    public IQueue getQueue() {
        return new GameQueue(this);
    }

    @Override
    public IServerQueue getServerQueue() {
        return new GameServerQueue(this);
    }
}
