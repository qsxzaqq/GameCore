package cc.i9mc.gamecore.queue.gameFactory;

import cc.i9mc.gamecore.queue.ServerInfo;
import cc.i9mc.gamecore.queue.ServerType;
import cc.i9mc.gamecore.queue.iFactory.IServerQueue;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by JinVan on 2021-01-30.
 */
public class GameServerQueue extends IServerQueue {

    private final GameBalancer gameBalancer;
    private final Random random = new Random();

    public GameServerQueue(GameBalancer gameBalancer) {
        super(gameBalancer);

        this.gameBalancer = gameBalancer;
    }

    @Override
    public void run() {
        if (gameBalancer.getServerLink() != null) {
            ServerInfo serverInfo = gameBalancer.getServers().get(gameBalancer.getServerLink());
            if (serverInfo.getServerType() == ServerType.WAITING && serverInfo.isAlive() && serverInfo.getPlayers() < serverInfo.getMaxPlayers()) {
                return;
            }
        }

        List<Map.Entry<String, ServerInfo>> collect = gameBalancer.getServers().entrySet().stream().filter(x -> x.getValue().getGameType().equals(gameBalancer.getType()) && x.getValue().getPlayers() < x.getValue().getMaxPlayers() && x.getValue().getServerType() == ServerType.WAITING && x.getValue().isAlive()).collect(Collectors.toList());
        if (collect.isEmpty()) {
            gameBalancer.setServerLink(null);
            return;
        }

        gameBalancer.setServerLink(collect.get(0).getValue().getPlayers() > 0 ? collect.get(0).getKey() : collect.get(random.nextInt(collect.size())).getKey());
    }
}
