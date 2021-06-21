package cc.i9mc.gamecore;

import cc.i9mc.gamecore.console.STDOUT;

public class CommandManager {
    public boolean command(String label, String[] args) {
        try {
            switch (label) {
                case "help":
                    STDOUT.info("attop/gameserver/xbwmap/bwmap/onebwmap/dan/debug/level");
                    return true;
                case "attop":
                    STDOUT.info(GameCore.getInstance().getAttopController().print());
                    return true;
                case "gameserver":
                    if (args.length == 0) {
                        STDOUT.info("gameserver reload/show <args>");
                        return true;
                    } else {
                        switch (args[0]) {
                            case "show":
                                if (args.length > 1) {
                                    GameCore.getInstance().getBalancerController().print(args[1]);
                                } else {
                                   GameCore.getInstance().getBalancerController().print();
                                }

                                return true;
                            case "reload":
                                //GameCore.getInstance().getQueueController().reload();
                                STDOUT.info("gameserver reload successful.");
                                return true;
                            default:
                                return false;
                        }
                    }
                case "xbwmap":
                    if (args.length == 0) {
                        STDOUT.info("xbwmap reload/show");
                        return true;
                    } else {
                        switch (args[0]) {
                            case "show":
                                STDOUT.info(GameCore.getInstance().getXbwMapController().print());

                                return true;
                            case "reload":
                                GameCore.getInstance().getXbwMapController().reload();
                                STDOUT.info("xbwmap reload successful.");
                                return true;
                            default:
                                return false;
                        }
                    }
                case "bwmap":
                    if (args.length == 0) {
                        STDOUT.info("bwmap reload/show");
                        return true;
                    } else {
                        switch (args[0]) {
                            case "show":
                                STDOUT.info(GameCore.getInstance().getBwMapController().print());
                                return true;
                            case "reload":
                                GameCore.getInstance().getBwMapController().reload();
                                STDOUT.info("bwmap reload successful.");
                                return true;
                            default:
                                return false;
                        }
                    }
                case "onebwmap":
                    if (args.length == 0) {
                        STDOUT.info("onebwmap reload/show");
                        return true;
                    } else {
                        switch (args[0]) {
                            case "show":
                                STDOUT.info(GameCore.getInstance().getOneBWMapController().print());
                                return true;
                            case "reload":
                                GameCore.getInstance().getOneBWMapController().reload();
                                STDOUT.info("onebwmap reload successful.");
                                return true;
                            default:
                                return false;
                        }
                    }
                case "dan":
                    if (args.length == 0) {
                        STDOUT.info("dan list/update/show <args>");
                        return true;
                    } else {
                        switch (args[0]) {
                            case "list":
                                STDOUT.info("dan size:" + GameCore.getInstance().getDanController().size());
                                return true;
                            case "update":
                                if (args.length < 2) {
                                    STDOUT.info("update <args>");
                                    return true;
                                }
                                GameCore.getInstance().getDanController().update(args[1]);
                                STDOUT.info("dan " + args[0] + " update successful.");
                                return true;
                            case "show":
                                if (args.length < 2) {
                                    STDOUT.info("show <args>");
                                    return true;
                                }
                                STDOUT.info("dan:" + GameCore.getInstance().getDanController().show(args[1]));
                                return true;
                            default:
                                return false;
                        }
                    }
                case "debug":
                    STDOUT.debug = !STDOUT.debug;
                    return true;
                case "level":
                    if (args.length > 0) {
                        STDOUT.currentLevel = Integer.parseInt(args[0]);
                        STDOUT.info("level now is" + STDOUT.currentLevel);
                    } else {
                        STDOUT.info("level <level>");
                    }
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
