package cc.i9mc.gamecore.controllers;

import cc.i9mc.gamecore.console.STDOUT;
import cc.i9mc.gamecore.utils.RedisUtil;
import com.google.gson.Gson;
import lombok.Data;
import redis.clients.jedis.JedisPubSub;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SIServerController {
    private static final Random random = new Random();
    private final Gson gson = new Gson();
    private final ExecutorService executorService;
    private final Timer timer;

    private final List<String> gameTypes;
    private final ConcurrentHashMap<String, ServerInfo> servers;
    private final HashMap<String, String> serverLink;

    public SIServerController() {
        timer = new Timer();
        executorService = Executors.newCachedThreadPool();

        gameTypes = new ArrayList<>();
        servers = new ConcurrentHashMap<>();
        serverLink = new HashMap<>();
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

        executorService.execute(() -> RedisUtil.get().subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                ServerData serverData = gson.fromJson(message, ServerData.class);

                ServerInfo serverInfo = getServerInfo(serverData);
                if (serverInfo == null && serverData.getServerType() != ServerType.END) {
                    serverInfo = register(serverData);
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
                        RedisUtil.publish("Management", "removeServer|ALL|" + serverInfo.getName() + "|" + serverData.getIp() + "|" + serverInfo.getGameType());
                        servers.remove(serverData.getName());
                        break;
                }
            }
        }, "GameServerACK"));

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                servers.entrySet().removeIf(stringServerInfoEntry -> Math.abs(System.currentTimeMillis() - stringServerInfoEntry.getValue().getLastACK()) > 10000);
            }
        }, 0, 1000);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (String type : gameTypes) {
                    if (serverLink.getOrDefault(type, null) != null) {
                        if (!servers.containsKey(serverLink.get(type))) {
                            continue;
                        }

                        ServerInfo serverInfo = servers.get(serverLink.get(type));
                        if (serverInfo.getServerType() == ServerType.WAITING && serverInfo.isAlive() && serverInfo.getPlayers() < serverInfo.getMaxPlayers()) {
                            continue;
                        }
                    }

                    List<Map.Entry<String, ServerInfo>> collect = servers.entrySet().stream().filter(x -> x.getValue().getGameType().equals(type) && x.getValue().getPlayers() < x.getValue().getMaxPlayers() && x.getValue().getServerType() == ServerType.WAITING && x.getValue().isAlive()).collect(Collectors.toList());
                    if (collect.isEmpty()) {
                        serverLink.put(type, null);
                        continue;
                    }
                    serverLink.put(type, collect.get(0).getValue().getPlayers() > 0 ? collect.get(0).getKey() : collect.get(random.nextInt(collect.size())).getKey());
                }
            }
        }, 0, 1000);

        executorService.execute(() -> RedisUtil.get().subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                QueueRequest queueRequest = gson.fromJson(message, QueueRequest.class);
                String serverName = serverLink.getOrDefault(queueRequest.getRedisName(), null);

                Queue queue = new Queue();
                queue.setServer(serverName);
                queue.setUuid(queueRequest.getUuid());

                String queueJson = gson.toJson(queue);
                STDOUT.info(queueJson, 1);
                RedisUtil.publish("Queue", queueJson);
            }
        }, "QueueRequest"));
    }

    public ServerInfo register(ServerData serverData) {
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
            serverInfo.setName("mini" + ips[2] + ips[3] + serverInfo.getGameType().substring(0, 1).toUpperCase());
        }


        RedisUtil.publish("Management", "addServer|ALL|" + serverInfo.getName() + "|" + serverInfo.getIp() + "|" + serverInfo.getGameType());
        if (!gameTypes.contains(serverInfo.getGameType())) {
            gameTypes.add(serverInfo.getGameType());
        }
        return servers.put(serverInfo.getName(), serverInfo);
    }

    public void reload() {

    }

    public String print() {
        StringBuilder stringBuilder = null;
        for (String type : gameTypes) {
            stringBuilder = printType(type, stringBuilder);
        }

        if(stringBuilder == null) {
            return "NONE";
        }

        return stringBuilder.toString();
    }

    public String print(String type) {
        return printType(type, null).toString();
    }

    public StringBuilder printType(String type, StringBuilder stringBuilder) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder("- " + type);
        } else {
            stringBuilder.append("\n- ").append(type);
        }

        for (Map.Entry<String, ServerInfo> e : servers.entrySet()) {
            if (!e.getValue().getGameType().equals(type)) {
                continue;
            }

            stringBuilder.append("\n - ").append(e.getValue());
        }

        return stringBuilder;
    }

    public ServerInfo getServerInfo(ServerData serverData) {
        for (ServerInfo serverInfo : servers.values()) {
            if (serverInfo.getGameType().equals(serverData.getGameType()) && serverInfo.getIp().equals(serverData.getIp())) {
                return serverInfo;
            }
        }

        return null;
    }

    public enum ServerType {
        STARTUP, WAITING, RUNNING, END
    }

    @Data
    public static class ServerInfo {
        private String gameType;
        private String name;
        private String ip;
        private ServerType serverType;
        private int players;
        private int maxPlayers;
        private Long lastACK;

        public boolean isAlive() {
            return Math.abs(System.currentTimeMillis() - lastACK) < 5000;
        }
    }

    @Data
    public static class ServerData {
        private ServerType serverType;
        private String gameType;
        private String ip;
        private String name;
        private int players;
        private int maxPlayers;
        private HashMap<String, Object> expand;
    }

    @Data
    public static class QueueRequest {
        private String redisName;
        private String uuid;
    }

    @Data
    public static class Queue {
        private String server;
        private String uuid;
    }
}
