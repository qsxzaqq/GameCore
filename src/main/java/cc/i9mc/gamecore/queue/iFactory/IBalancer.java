package cc.i9mc.gamecore.queue.iFactory;

import cc.i9mc.gamecore.queue.ServerData;
import cc.i9mc.gamecore.queue.ServerInfo;
import cc.i9mc.gamecore.queue.ServerType;
import cc.i9mc.gamecore.utils.RedisUtil;
import lombok.Data;
import redis.clients.jedis.JedisPubSub;

import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by JinVan on 2021-01-30.
 */
@Data
public abstract class IBalancer {
    private String type;
    private String perfix;

    private ExecutorService executorService;
    private Timer timer;

    private ConcurrentHashMap<String, ServerInfo> servers;
    private String serverLink;

    public IBalancer(String type, String perfix) {
        this.type = type;
        this.perfix = perfix;

        this.executorService = Executors.newCachedThreadPool();
        this.timer = new Timer();

        this.servers = new ConcurrentHashMap<>();

        run();
    }

    public void run() {
        executorService.execute(() -> RedisUtil.get().subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                StringTokenizer in = new StringTokenizer(message, "|");
                if (!in.nextToken().equals("ResendGameServer")) {
                    return;
                }

                String serverid = in.nextToken();
                for (ServerInfo serverInfo : servers.values()) {
                    if (serverInfo.isAlive()) {
                        RedisUtil.publish("Management", "addServer|" + serverid + "|" + serverInfo.getName() + "|" + serverInfo.getIp() + "|" + serverInfo.getGameType());
                    }
                }
            }
        }, "Management"));

        executorService.execute(() -> RedisUtil.get().subscribe(getServerACK(), "GameServerACK"));
        timer.schedule(getServerQueue(), 0, 1000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                servers.entrySet().removeIf((entry) -> Math.abs(System.currentTimeMillis() - entry.getValue().getLastACK()) > 10000L);
            }
        }, 0, 1000);
        executorService.execute(() -> RedisUtil.get().subscribe(getQueue(), "QueueRequest"));
    }

    public abstract IServerACK getServerACK();
    public abstract IQueue getQueue();
    public abstract IServerQueue getServerQueue();

    public ServerInfo getServerInfo(ServerData serverData) {
        for (ServerInfo serverInfo : servers.values()) {
            if (serverInfo.getGameType().equals(serverData.getGameType()) && serverInfo.getIp().equals(serverData.getIp())) {
                return serverInfo;
            }
        }

        return null;
    }

    public ServerInfo registerServer(ServerData serverData) {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setGameType(serverData.getGameType());
        serverInfo.setIp(serverData.getIp());
        serverInfo.setServerType(ServerType.STARTUP);
        serverInfo.setLastACK(System.currentTimeMillis());
        serverInfo.setPlayers(0);
        serverInfo.setMaxPlayers(0);

        if (serverInfo.getName() != null) {
            serverInfo.setName(serverData.getName());
        }else {
            String[] ips = serverInfo.getIp().split(":")[0].split("\\.");
            serverInfo.setName(perfix + ips[2] + ips[3] + serverInfo.getGameType().substring(0, 1).toUpperCase());
        }


        RedisUtil.publish("Management", "addServer|ALL|" + serverInfo.getName() + "|" + serverInfo.getIp() + "|" + serverInfo.getGameType());
        return servers.put(serverInfo.getName(), serverInfo);
    }

    public void removeServer(ServerData serverData) {
        RedisUtil.publish("Management", "removeServer|ALL|" + serverData.getName() + "|" + serverData.getIp() + "|" + serverData.getGameType());
        servers.remove(serverData.getName());
    }

    public int getServerPlayers() {
        int count = 0;
        for (ServerInfo serverInfo : servers.values()) {
            count += serverInfo.getPlayers();
        }

        return count;
    }

    public int getServerMaxPlayers() {
        int count = 0;
        for (ServerInfo serverInfo : servers.values()) {
            count += serverInfo.getMaxPlayers();
        }

        return count;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(ServerInfo info : servers.values()){
            builder
                .append("**")
                .append(info.getName())
                .append(": ")
                .append(info.getPlayers())
                .append("/")
                .append(info.getMaxPlayers())
                .append("** ");
        }

        return builder.toString();
    }

    public void destroy() {
        try {
            getServerACK().unsubscribe();
            getQueue().unsubscribe();
        } catch (Exception ignored) {
        }

        executorService.shutdown();
        timer.cancel();
    }
}
