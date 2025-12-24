import elements.Client;
import elements.cli.ServerCommands;

public class ClientMain {

    public static void main(String[] args) {
        ServerCommands.initGeneral();
        Client.launch();
    }
}
