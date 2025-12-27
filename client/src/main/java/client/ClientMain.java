package client;

import client.elements.Client;
import client.elements.InputManager;
import client.elements.OutputManager;
import utils.elements.ClientTypes;
import client.elements.cli.ServerCommands;

public class ClientMain {

    public static void main(String[] args) {
        ServerCommands.initGeneral();
        Client.launch(ClientTypes.CONSOLE, args[0], Integer.parseInt(args[1]));
        InputManager.startInputThread();
        OutputManager.addOutPutListener(System.out::print);
    }
}
