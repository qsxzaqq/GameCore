package cc.i9mc.gamecore.queue;

import lombok.Data;

import java.util.HashMap;

/**
 * Created by JinVan on 2021-01-30.
 */
@Data
public class ServerData {
    private ServerType serverType;
    private String gameType;
    private String ip;
    private String name;
    private int players;
    private int maxPlayers;
    private HashMap<String, Object> expand;
}
