package cc.i9mc.gamecore.queue.iFactory;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.TimerTask;

/**
 * Created by JinVan on 2021-01-30.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class IServerQueue extends TimerTask {

    private IBalancer balancer;

    public IServerQueue(IBalancer balancer) {
        this.balancer = balancer;
    }
}
