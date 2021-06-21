package cc.i9mc.gamecore.controllers;

import cc.i9mc.gamecore.console.STDOUT;
import cc.i9mc.gamecore.utils.BWSQLUtil;
import cc.i9mc.gamecore.utils.KBSQLUtil;
import cc.i9mc.gamecore.utils.PlayerSQLUtil;
import cc.i9mc.gamecore.utils.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DanController {
    private final ExecutorService executorService;

    public DanController() {
        executorService = Executors.newCachedThreadPool();
    }

    public static int getPlayerKBLevel(String name) {
        int level = 0;

        try {
            Connection connection = KBSQLUtil.getInstance().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM kitbattle Where player_name=?");
            preparedStatement.setString(1, name);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                level = resultSet.getInt("Kills") * 2;
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return level;
    }

    public static int getPlayerBWLevel(String name) {
        int level = 0;

        try {
            Connection connection = BWSQLUtil.getInstance().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM bw_stats_players Where Name=?");
            preparedStatement.setString(1, name);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                level = (resultSet.getInt("kills") * 2) + (resultSet.getInt("destroyedBeds") * 10) + (resultSet.getInt("wins") * 15);
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return level;
    }

    public static int getPlayerData(String name) {
        int level = 0;

        try {
            Connection connection = PlayerSQLUtil.getInstance().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM danStats Where Name=?");
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                level = resultSet.getInt("Level");
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return level;
    }

    public static void addPlayerLevel(String player, int level) {
        Connection connection = null;
        ResultSet resultSet = null;

        try {
            connection = PlayerSQLUtil.getInstance().getConnection();
            if (connection == null || connection.isClosed()) {
                return;
            }

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM danStats Where Name=?");
            preparedStatement.setString(1, player);
            resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                preparedStatement = connection.prepareStatement("INSERT INTO danStats (Name,Level) VALUES (?,?)");
                preparedStatement.setString(1, player);
                preparedStatement.setInt(2, level);
                preparedStatement.executeUpdate();
            } else {
                preparedStatement = connection.prepareStatement("UPDATE danStats SET Level=? Where Name=?");
                preparedStatement.setInt(1, resultSet.getInt("Level") + level);
                preparedStatement.setString(2, player);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        executorService.execute(() -> RedisUtil.get().subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (channel.equalsIgnoreCase("DanUpdate")) {
                    String[] args = message.split(", ");
                    if (args[0].equals("addLevel")) {
                        int level = Integer.parseInt(args[2]);
                        addPlayerLevel(args[1], level);
                        return;
                    }

                    if (!args[0].equals("add")) {
                        return;
                    }

                    int bw = getPlayerBWLevel(args[1]);
                    int kb = getPlayerKBLevel(args[1]);

                    Map<String, String> map = new HashMap<>();
                    map.put("BW", String.valueOf(bw));
                    map.put("KB", String.valueOf(kb));
                    map.put("level", String.valueOf(bw + kb + getPlayerData(args[1])));
                    map.put("time", String.valueOf(System.currentTimeMillis()));
                    STDOUT.info("add player dan " + args[1] + " data...", 1);

                    Jedis jedis = RedisUtil.get();
                    jedis.hmset("dan:players:" + args[1], map);
                    jedis.expire("dan:players:" + args[1], 43200);
                    jedis.close();
                    RedisUtil.publish("DanUpdate", "update, " + args[1]);
                }
            }
        }, "DanUpdate"));

    }

    public int size() {
        Jedis jedis = RedisUtil.get();
        int size = jedis.keys("dan:players:*").size();
        jedis.close();

        return size;
    }

    public void update(String name) {
        Jedis jedis = RedisUtil.get();
        jedis.del("dan:players:" + name);
        int bw = getPlayerBWLevel(name);
        int kb = getPlayerKBLevel(name);
        Map<String, String> map = new HashMap<>();
        map.put("BW", String.valueOf(bw));
        map.put("KB", String.valueOf(kb));
        map.put("level", String.valueOf(bw + kb + getPlayerData(name)));
        map.put("time", String.valueOf(System.currentTimeMillis()));
        jedis.hmset("dan:players:" + name, map);
        jedis.close();
    }

    public Map<String, String> show(String name) {
        Map<String, String> map = new HashMap<>();
        Jedis jedis = RedisUtil.get();
        if (jedis.exists("dan:players:" + name)) {
            map = jedis.hgetAll("dan:players:" + name);
        }
        jedis.close();

        return map;
    }
}
