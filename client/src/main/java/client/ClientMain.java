package client;

import client.elements.Client;
import client.elements.InputManager;
import client.elements.OutputManager;
import utils.elements.ClientTypes;
import client.elements.cli.ServerCommands;

public class ClientMain {

    public static void main(String[] args) {
        ServerCommands.initGeneral();
        OutputManager.addOutPutListener(System.out::print);
        Client.launch(ClientTypes.CONSOLE);
        InputManager.startInputThread();
    }
}
