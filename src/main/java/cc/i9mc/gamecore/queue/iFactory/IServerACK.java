package cc.i9mc.gamecore.queue.iFactory;

import cc.i9mc.gamecore.GameCore;
import cc.i9mc.gamecore.console.STDOUT;
import cc.i9mc.gamecore.queue.ServerData;
import cc.i9mc.gamecore.queue.ServerInfo;
import cc.i9mc.gamecore.queue.ServerType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by JinVan on 2021-01-30.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class IServerACK extends JedisPubSub {

    private IBalancer balancer;

    public IServerACK(IBalancer balancer) {
        this.balancer = balancer;
    }

    @Override
    public void onMessage(String channel, String message) {
        ServerData serverData = GameCore.GSON.fromJson(message, ServerData.class);
        if (!serverData.getGameType().equals(balancer.getType())) {
            return;
        }

        ServerInfo serverInfo = balancer.getServerInfo(serverData);
        if (serverInfo == null && serverData.getServerType() != ServerType.END) {
            serverInfo = balancer.registerServer(serverData);
            STDOUT.info("reg " + serverData.getGameType() + " - " + serverData.getIp() + " - " + serverData.getName());
        }

        if (serverInfo == null) {
            return;
        }

        switch (serverData.getServerType()) {
            case WAITING:
            case RUNNING:
                serverInfo.setServerType(serverData.getServerType());
                serverInfo.setPlayers(serverData.getPlayers());
                serverInfo.setMaxPlayers(serverData.getMaxPlayers());
                serverInfo.setLastACK(System.currentTimeMillis());
                break;
            case END:
                balancer.removeServer(serverData);
                break;
        }
    }
}
