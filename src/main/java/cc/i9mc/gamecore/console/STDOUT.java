package cc.i9mc.gamecore.console;

import cc.i9mc.gamecore.GameCore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class STDOUT {
    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat formatYM = new SimpleDateFormat("yy-MM-dd-HH-mm-ss ");
    private static final Logger logger = Logger.getLogger("GameCore");
    public static boolean debug = false;
    public static int currentLevel = 0;

    public static void info(String info) {
        print(cc.i9mc.gamecore.console.Level.INFO, info, 0);
    }

    public static void info(String info, int depth) {
        print(cc.i9mc.gamecore.console.Level.INFO, info, depth);
    }

    public static void warn(String info) {
        print(cc.i9mc.gamecore.console.Level.WARN, info, 0);
    }

    public static void warn(String info, int depth) {
        print(cc.i9mc.gamecore.console.Level.WARN, info, depth);
    }

    public static void error(String info) {
        print(cc.i9mc.gamecore.console.Level.ERROR, info, 0);
    }

    public static void error(String info, int depth) {
        print(cc.i9mc.gamecore.console.Level.ERROR, info, depth);
    }

    public static void debug(String info) {
        print(cc.i9mc.gamecore.console.Level.DEBUG, info, 0);
    }

    public static void debug(String info, int depth) {
        print(cc.i9mc.gamecore.console.Level.DEBUG, info, depth);
    }


    public static void print(cc.i9mc.gamecore.console.Level level, String info, int depth) {
        if (level == cc.i9mc.gamecore.console.Level.DEBUG && !debug)
            return;
        if (depth > currentLevel) {
            return;
        }
        GameCore.getInstance().getConsole().getLineReader().printAbove(format.format(Calendar.getInstance().getTime()) + " [" + level.toString() + "] " + info);
        logger.log(Level.INFO, formatYM.format(Calendar.getInstance().getTime()) + " [" + level.toString() + "] " + info);
    }


    public static void setUp() {
        try {
            File file = new File("logs");
            if (!file.exists()) {
                file.mkdir();
            }

            FileHandler fh = new FileHandler(file.getName() + File.separator + formatYM.format(Calendar.getInstance().getTime()) + ".log", 100000000, 1, true);
            for (Handler h : logger.getHandlers()) {
                logger.removeHandler(h);
            }
            logger.setUseParentHandlers(false);
            logger.addHandler(fh);
            fh.setFormatter(new MyFormatter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
