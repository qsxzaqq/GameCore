package cc.i9mc.gamecore.queue.lobbyFactory;

import cc.i9mc.gamecore.GameCore;
import cc.i9mc.gamecore.console.STDOUT;
import cc.i9mc.gamecore.queue.Queue;
import cc.i9mc.gamecore.queue.iFactory.IQueue;
import cc.i9mc.gamecore.utils.RedisUtil;
import redis.clients.jedis.Jedis;

/**
 * Created by JinVan on 2021-01-30.
 */
public class LobbyQueue extends IQueue {

    private final LobbyBalancer lobbyBalancer;

    public LobbyQueue(LobbyBalancer lobbyBalancer) {
        super(lobbyBalancer);
        this.lobbyBalancer = lobbyBalancer;
    }

    @Override
    public void onMessage(String channel, String message) {
        Queue.Request queueRequest = GameCore.GSON.fromJson(message, Queue.Request.class);
        if (!queueRequest.getRedisName().equals(lobbyBalancer.getType())) {
            return;
        }

        String serverName = lobbyBalancer.getServerLink();

        Queue.Result queue = new Queue.Result();
        queue.setServer(serverName);
        queue.setUuid(queueRequest.getUuid());

        String queueJson = GameCore.GSON.toJson(queue);
        STDOUT.info(queueJson, 1);
        RedisUtil.publish("Send", queueJson);

        try (Jedis jedis = RedisUtil.get()) {
            jedis.set("GC.LastLobby." + queue.getUuid(), lobbyBalancer.getType());
        }
    }
}
