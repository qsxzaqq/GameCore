package cc.i9mc.gamecore.queue.lobbyFactory;

import cc.i9mc.gamecore.queue.iFactory.IServerACK;

/**
 * Created by JinVan on 2021-01-30.
 */
public class LobbyServerACK extends IServerACK {

    public LobbyServerACK(LobbyBalancer lobbyBalancer) {
        super(lobbyBalancer);
    }
}
