package cc.i9mc.gamecore.console;

import cc.i9mc.gamecore.CommandManager;
import cc.i9mc.gamecore.GameCore;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Console {
    private final CommandManager manager;
    @Getter
    private final LineReader lineReader;
    @Getter
    private final Terminal terminal;

    @SneakyThrows
    public Console(CommandManager manager) {
        this.manager = manager;

        terminal = TerminalBuilder.builder().system(true).build();
        lineReader = LineReaderBuilder.builder().terminal(terminal).completer(new NullCompleter()).build();
    }

    public void run() {
        String line = lineReader.readLine("> ");

        while (!line.equals("exit")) {
            StringTokenizer token = new StringTokenizer(line, " ");
            if (!token.hasMoreTokens()) {
                line = lineReader.readLine("> ");
                continue;
            }
            String label = token.nextToken();
            ArrayList<String> args = new ArrayList<>();
            while (token.hasMoreTokens()) {
                args.add(token.nextToken());
            }
            String[] str = args.toArray(new String[0]);
            if (!manager.command(label, str)) {
                STDOUT.print(Level.ERROR, "未知指令!", 0);
            }
            line = lineReader.readLine("> ");
        }
        STDOUT.info("跑路啦...", 0);
        GameCore.getInstance().shutDown();
    }
}
