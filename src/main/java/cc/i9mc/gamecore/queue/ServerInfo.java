package cc.i9mc.gamecore.queue;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by JinVan on 2021-01-30.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServerInfo extends ServerData {
    private Long lastACK;

    public boolean isAlive() {
        return Math.abs(System.currentTimeMillis() - lastACK) < 5000;
    }
}
