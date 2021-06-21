package cc.i9mc.gamecore.queue;

import cc.i9mc.gamecore.console.STDOUT;
import cc.i9mc.gamecore.queue.gameFactory.GameBalancer;
import cc.i9mc.gamecore.queue.iFactory.IBalancer;
import cc.i9mc.gamecore.queue.lobbyFactory.LobbyBalancer;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class BalancerController {
    private final List<IBalancer> balancers;

    public BalancerController() {
        this.balancers = new ArrayList<>();
    }

    public void loadConfig() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        Map<String, Map<String, Object>> map = yaml.loadAs(new FileReader(new File("queue.yaml")), LinkedHashMap.class);

        for(String type : map.keySet()) {
            switch (type) {
                case "game":
                    for (String server : (ArrayList<String>) map.get(type).get("servers")) {
                        balancers.add(new GameBalancer(server, (String) map.get(type).get("perfix")));
                    }
                    break;
                case "lobby":
                    for (String server : (ArrayList<String>) map.get(type).get("servers")) {
                        balancers.add(new LobbyBalancer(server, (String) map.get(type).get("perfix")));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void close(){
        Iterator<IBalancer> iterator = balancers.iterator();
        while(iterator.hasNext()){
            iterator.next().destroy();
            iterator.remove();
        }
    }

    public void print(String name){
        for(IBalancer balancer : balancers){
            if(balancer.getType().contains(name)){
                STDOUT.info("- " + balancer.getType(),0);
                STDOUT.info(" - Players: " + balancer.getServerPlayers() + "/" + balancer.getServerMaxPlayers());
                STDOUT.info(" - Servers: " + balancer.toString(),0);
            }
        }
    }

    public void print(){
        for(IBalancer balancer : balancers){
            STDOUT.info("- " + balancer.getType(),0);
            STDOUT.info( " - Players: "+ balancer.getServerPlayers() + "/" + balancer.getServerMaxPlayers());
            STDOUT.info(" - Servers: " + balancer.toString(),0);
        }
    }
}
