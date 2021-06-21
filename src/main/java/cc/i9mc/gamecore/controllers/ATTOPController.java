package cc.i9mc.gamecore.controllers;

import cc.i9mc.gamecore.console.STDOUT;
import cc.i9mc.gamecore.utils.PlayerSQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ATTOPController {
    private final Timer timer;

    public ATTOPController() {
        timer = new Timer();
    }

    public static Date getMondayOfThisWeek() {
        Calendar calendar = Calendar.getInstance();
        int day_of_week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (day_of_week == 0)
            day_of_week = 7;
        calendar.add(Calendar.DATE, -day_of_week + 1);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, -7);
        return calendar.getTime();
    }


    public static Date getSundayOfThisWeek() {
        Calendar calendar = Calendar.getInstance();
        int day_of_week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (day_of_week == 0)
            day_of_week = 7;
        calendar.add(Calendar.DATE, -day_of_week + 7);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.add(Calendar.DATE, -7);
        return calendar.getTime();
    }

    public void run() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Calendar c = Calendar.getInstance();
                int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
                if (day_of_week == 0)
                    day_of_week = 7;
                int currentH = c.get(Calendar.HOUR_OF_DAY);
                int currentMin = c.get(Calendar.MINUTE);
                int currentS = c.get(Calendar.SECOND);

                if (day_of_week == 1 && currentH == 0 && 30 - currentMin == 0 && currentS == 0) {
                    HashMap<String, Integer> points = getWeekPlayers();
                    if (points == null) return;
                    List<Map.Entry<String, Integer>> list = new ArrayList<>(points.entrySet());
                    list.sort((o1, o2) -> o2.getValue() - o1.getValue());

                    System.out.println(list);

                    int i = 1;
                    for (Map.Entry<String, Integer> e : list) {
                        if (i > 12) {
                            continue;
                        }

                        if (i == 1) {
                            addPoint(e.getKey(), ((int) (e.getValue() * 0.1)) + 2000);
                        } else if (i == 2) {
                            addPoint(e.getKey(), ((int) (e.getValue() * 0.08)) + 1000);
                        } else if (i == 3) {
                            addPoint(e.getKey(), ((int) (e.getValue() * 0.06)) + 1000);
                        } else if (i >= 4 && i <= 8) {
                            addPoint(e.getKey(), ((int) (e.getValue() * 0.03)) + 500);
                        } else {
                            addPoint(e.getKey(), 600);
                        }


                        STDOUT.info("give " + e.getKey() + " points " + e.getValue(), 1);
                        i++;
                    }
                }
            }
        }, 1000, 1000);
    }

    public String print() {
        StringBuilder stringBuilder = null;
        HashMap<String, Integer> points = getWeekPlayers();
        if (points == null) return null;
        List<Map.Entry<String, Integer>> list = new ArrayList<>(points.entrySet());
        list.sort((o1, o2) -> o2.getValue() - o1.getValue());
        int i = 1;
        for (Map.Entry<String, Integer> e : list) {
            if (i >= 12) {
                continue;
            }

            if (stringBuilder == null) {
                stringBuilder = new StringBuilder("- " + e.getKey() + " " + e.getValue());
            } else {
                stringBuilder.append("\n- ").append(e.getKey()).append(" ").append(e.getValue());
            }
            i++;
        }
        return stringBuilder.toString();
    }

    private HashMap<String, Integer> getWeekPlayers() {
        Connection connection = null;
        ResultSet resultSet = null;
        HashMap<String, Integer> uuids = new HashMap<>();

        try {
            connection = PlayerSQLUtil.getInstance().getConnection();
            if (connection == null || connection.isClosed()) {
                return null;
            }

            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `AccumulatedTopDay` WHERE `date` >= ? AND `date` <= ?");
            ps.setLong(1, getMondayOfThisWeek().getTime());
            ps.setLong(2, getSundayOfThisWeek().getTime());
            resultSet = ps.executeQuery();


            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                int point = resultSet.getInt("point");

                uuids.put(uuid, uuids.getOrDefault(uuid, 0) + point);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return uuids;
    }

    private HashMap<String, Integer> getDayPlayers() {
        Connection connection = null;
        ResultSet resultSet = null;
        HashMap<String, Integer> uuids = new HashMap<>();

        try {
            connection = PlayerSQLUtil.getInstance().getConnection();
            if (connection == null || connection.isClosed()) {
                return null;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) - 1, 0, 0, 0);
            long start = calendar.getTime().getTime();

            calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) - 1, 23, 59, 59);
            long end = calendar.getTime().getTime();

            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `AccumulatedTopDay` WHERE `date` >= ? AND `date` <= ?");
            ps.setLong(1, start);
            ps.setLong(2, end);
            resultSet = ps.executeQuery();


            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                int point = resultSet.getInt("point");

                uuids.put(uuid, uuids.getOrDefault(uuid, 0) + point);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return uuids;
    }

    private void addPoint(String uuid, int point) {
        STDOUT.info("give " + uuid + " points " + point);

        Connection connection = null;
        ResultSet resultSet = null;

        try {
            connection = PlayerSQLUtil.getInstance().getConnection();
            if (connection == null || connection.isClosed()) {
                return;
            }

            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `playerpoints` WHERE playername = ?");
            ps.setString(1, uuid);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                ps = connection.prepareStatement("UPDATE playerpoints SET points=? Where playername=?");
                ps.setInt(1, resultSet.getInt("points") + point);
                ps.setString(2, uuid);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
