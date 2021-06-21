package cc.i9mc.gamecore.queue.iFactory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import redis.clients.jedis.JedisPubSub;


/**
 * Created by JinVan on 2021-01-30.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class IQueue extends JedisPubSub {

    private IBalancer balancer;

    public IQueue(IBalancer balancer) {
        this.balancer = balancer;
    }
}
