package cc.i9mc.gamecore.queue.lobbyFactory;

import cc.i9mc.gamecore.queue.ServerInfo;
import cc.i9mc.gamecore.queue.ServerType;
import cc.i9mc.gamecore.queue.iFactory.IServerQueue;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by JinVan on 2021-01-30.
 */
public class LobbyServerQueue extends IServerQueue {

    private final LobbyBalancer lobbyBalancer;
    private final Random random = new Random();
    
    public LobbyServerQueue(LobbyBalancer lobbyBalancer) {
        super(lobbyBalancer);
        
        this.lobbyBalancer = lobbyBalancer;
    }

    @Override
    public void run() {
        List<Map.Entry<String, ServerInfo>> collect = lobbyBalancer.getServers().entrySet().stream().filter(x -> x.getValue().getGameType().equals(lobbyBalancer.getType()) && x.getValue().getPlayers() < x.getValue().getMaxPlayers() && x.getValue().getServerType() == ServerType.WAITING && x.getValue().isAlive()).collect(Collectors.toList());
        if (collect.isEmpty()) {
            lobbyBalancer.setServerLink(null);
            return;
        }
        collect.sort(Comparator.comparingInt(o -> o.getValue().getPlayers()));
        lobbyBalancer.setServerLink(collect.get(0).getKey());
    }
}
