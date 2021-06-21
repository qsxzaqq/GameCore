package cc.i9mc.gamecore.utils;

import cc.i9mc.gamecore.console.STDOUT;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    private static JedisPool pool = null;

    public static redis.clients.jedis.Jedis get() {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(2048);
            pool = new JedisPool(config, "yxsj-redis", 6380, 0);

        }
        if (pool.getNumActive() > 30 || pool.getNumWaiters() > 0) {
            STDOUT.warn("Pool size warm, active: " + pool.getNumActive() + " waiters:" + pool.getNumWaiters());
        }
        return pool.getResource();
    }

    public static void publish(String channel, String msg) {
        Jedis jedis = get();
        jedis.publish(channel, msg);
        jedis.close();
    }
}
