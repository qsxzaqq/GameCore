package cc.i9mc.gamecore.controllers;

import cc.i9mc.gamecore.console.STDOUT;
import cc.i9mc.gamecore.queue.ServerData;
import cc.i9mc.gamecore.queue.ServerType;
import cc.i9mc.gamecore.utils.BWDataSQLUtil;
import cc.i9mc.gamecore.utils.RedisUtil;
import com.google.gson.Gson;
import redis.clients.jedis.JedisPubSub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OneBWMapController {
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, List<String>> servers;
    private final ConcurrentHashMap<String, String> serverWaits;
    private final Gson gson;
    private String last;

    public OneBWMapController() {
        executorService = Executors.newCachedThreadPool();
        serverWaits = new ConcurrentHashMap<>();
        servers = new ConcurrentHashMap<>();
        gson = new Gson();
        loadMaps();
    }

    public void run() {
        executorService.execute(() -> RedisUtil.get().subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                ServerData serverData = gson.fromJson(message, ServerData.class);

                if (!serverData.getGameType().equals("OneBedWars")) {
                    return;
                }

                if ((serverData.getServerType() == ServerType.WAITING || serverData.getServerType() == ServerType.RUNNING) && !serverData.getExpand().isEmpty() && serverData.getExpand().containsKey("map")) {
                    if (servers.containsKey((String) serverData.getExpand().get("map")) && !servers.get((String) serverData.getExpand().get("map")).contains(serverData.getIp())) {
                        servers.get((String) serverData.getExpand().get("map")).add(serverData.getIp());
                        serverWaits.remove(serverData.getIp());
                    }
                }

                if (serverData.getServerType() == ServerType.END && !serverData.getExpand().isEmpty() && serverData.getExpand().containsKey("map")) {
                    if (servers.containsKey((String) serverData.getExpand().get("map"))) {
                        servers.get((String) serverData.getExpand().get("map")).remove(serverData.getIp());
                    }
                    serverWaits.remove(serverData.getIp());
                }

                if (serverData.getServerType() == ServerType.STARTUP && !serverData.getExpand().containsKey("map")) {
                    String server;

                    if (serverWaits.containsKey(serverData.getIp())) {
                        server = serverWaits.get(serverData.getIp());
                    }else {
                        List<Map.Entry<String, List<String>>> list = new ArrayList<>(servers.entrySet());
                        list.sort((Comparator<? super Map.Entry<String, List<String>>>) (o1, o2) -> {
                            Integer integer = o1.getValue().size();
                            if (o1.getKey().equals(last)) {
                                integer = integer + 1;
                            }

                            return integer.compareTo(o2.getValue().size());
                        });
                        server = last = list.get(0).getKey();
                        serverWaits.put(serverData.getIp(), server);
                    }

                    RedisUtil.publish("MINIGame.OneBW." + serverData.getIp(), server);
                }
            }
        }, "GameServerACK"));
    }

    public void loadMaps() {
        servers.clear();
        try {
            Connection connection = BWDataSQLUtil.getInstance().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM OneBWMaps;");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                servers.put(resultSet.getString("MapName"), new ArrayList<>());
            }

            STDOUT.info("Load Game Maps " + servers.size());

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        loadMaps();
    }

    public String print() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : servers.entrySet()) {
            stringBuilder.append(entry.getKey()).append(" - ").append(entry.getValue().size()).append("\n");
        }

        return stringBuilder.toString();
    }
}
