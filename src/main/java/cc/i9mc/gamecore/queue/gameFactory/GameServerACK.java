package cc.i9mc.gamecore.queue.gameFactory;

import cc.i9mc.gamecore.queue.iFactory.IServerACK;

/**
 * Created by JinVan on 2021-01-30.
 */
public class GameServerACK extends IServerACK {

    public GameServerACK(GameBalancer gameBalancer) {
        super(gameBalancer);
    }
}
