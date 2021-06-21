package cc.i9mc.gamecore;

import cc.i9mc.gamecore.console.Console;
import cc.i9mc.gamecore.console.STDOUT;
import cc.i9mc.gamecore.controllers.*;
import cc.i9mc.gamecore.queue.BalancerController;
import com.google.gson.Gson;
import lombok.Getter;

import java.io.FileNotFoundException;

@Getter
public class GameCore {
    @Getter
    private static GameCore instance;
    private final Long startTime = System.currentTimeMillis();
    @Getter
    private final Console console;
    public static Gson GSON = new Gson();
    private CommandManager commandManager = new CommandManager();

    @Getter
    private ATTOPController attopController;
    @Getter
    private DanController danController;
    @Getter
    private BalancerController balancerController;
    @Getter
    private XBWMapController xbwMapController;
    @Getter
    private BWMapController bwMapController;
    @Getter
    private OneBWMapController oneBWMapController;

    public GameCore() {
        instance = this;
        console = new Console(commandManager);
        STDOUT.setUp();
        STDOUT.info("Server starting ...");
        setUp();
        STDOUT.info("Start up done.");
        STDOUT.info("loading to run.");
        loadRun();
        STDOUT.info("Load run done.");
        STDOUT.info("Done (" + Math.abs(System.currentTimeMillis() - startTime) + "ms)! For help, type \"help\"");

        openConsole();
    }


    private void openConsole() {
        console.run();
    }

    public void loadRun() {
        attopController.run();
        danController.run();
        try {
            balancerController.loadConfig();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        xbwMapController.run();
        bwMapController.run();
        oneBWMapController.run();
    }

    private void setUp() {
        commandManager = new CommandManager();
        STDOUT.info(" - command manager start up.");
        attopController = new ATTOPController();
        STDOUT.info(" - attop controller start up.");
        danController = new DanController();
        STDOUT.info(" - dan controller start up.");
        balancerController = new BalancerController();
        STDOUT.info(" - game server controller start up.");
        xbwMapController = new XBWMapController();
        STDOUT.info(" - xbw map controller start up.");
        bwMapController = new BWMapController();
        STDOUT.info(" - bw map controller start up.");
        oneBWMapController = new OneBWMapController();
        STDOUT.info(" - one bw map controller start up.");
    }

    public void shutDown() {
        balancerController.close();

        System.out.println("Bye~");
        System.exit(1);
    }
}
