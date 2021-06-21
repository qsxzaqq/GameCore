package cc.i9mc.gamecore.queue.gameFactory;

import cc.i9mc.gamecore.GameCore;
import cc.i9mc.gamecore.console.STDOUT;
import cc.i9mc.gamecore.queue.Queue;
import cc.i9mc.gamecore.queue.iFactory.IQueue;
import cc.i9mc.gamecore.utils.RedisUtil;

/**
 * Created by JinVan on 2021-01-30.
 */
public class GameQueue extends IQueue {

    private final GameBalancer gameBalancer;

    public GameQueue(GameBalancer gameBalancer) {
        super(gameBalancer);
        this.gameBalancer = gameBalancer;
    }

    @Override
    public void onMessage(String channel, String message) {
        Queue.Request queueRequest = GameCore.GSON.fromJson(message, Queue.Request.class);
        if (!queueRequest.getRedisName().equals(gameBalancer.getType())) {
            return;
        }

        String serverName = gameBalancer.getServerLink();

        Queue.Result queue = new Queue.Result();
        queue.setServer(serverName);
        queue.setUuid(queueRequest.getUuid());

        String queueJson = GameCore.GSON.toJson(queue);
        STDOUT.info(queueJson, 1);
        RedisUtil.publish("Queue", queueJson);
    }
}
