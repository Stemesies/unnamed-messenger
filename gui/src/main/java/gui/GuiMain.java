package gui;

import client.elements.Client;
import javafx.application.Application;
import utils.elements.ClientTypes;

import java.util.concurrent.CompletableFuture;

public class GuiMain {

    public static void main(String[] args) {
        CompletableFuture.supplyAsync(() -> {
            Client.launch(ClientTypes.GUI, args[0], Integer.parseInt(args[1]));
            return null;
        });
        Application.launch(ClientApplication.class);
    }
}
