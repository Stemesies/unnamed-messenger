import elements.Client;
import elements.ClientTypes;
import elements.cli.ServerCommands;

public class ClientMain {

    public static void main(String[] args) {
        ServerCommands.initGeneral();
        Client.launch(ClientTypes.CONSOLE);
    }
}
