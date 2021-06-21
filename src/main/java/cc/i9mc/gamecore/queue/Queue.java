package cc.i9mc.gamecore.queue;

import lombok.Data;

/**
 * Created by JinVan on 2021-01-30.
 */
public class Queue {
    @Data
    public static class Request {
        private String redisName;
        private String uuid;
    }

    @Data
    public static class Result {
        private String server;
        private String uuid;
    }
}
