package server;

import server.elements.Client;
import utils.network.SimpleServerSocket;

import java.util.ArrayList;
import java.util.Scanner;

import static server.elements.ServerData.getClients;

public class ServerMain {
    SimpleServerSocket socket = null;
    private final Scanner in = new Scanner(System.in);

    public void start() {
        socket = new SimpleServerSocket(8080);
        if (socket.isClosed())
            return;

        System.out.println("Server started");

        acceptClients();
        processInput();
    }

    private void acceptClients() {
        new Thread(() -> {
            while (socket != null) {
                var clSocket = socket.accept();
                if (clSocket == null)
                    continue;
                var client = new Client(clSocket);
                System.out.println("new client");
                getClients().add(client);
                processClient(client);
            }
        }).start();
    }

    public void stop() {
        broadcast("Closing server...");

        new ArrayList<>(getClients()).forEach(Client::close);
        System.out.println("Server closed.");
        socket.close();
        System.exit(0);
    }

    /**
     * Вещает сообщение всем подключенным пользователям.
     *
     * @param message сообщение для вещания
     */
    public void broadcast(String message) {
        for (var client : getClients())
            client.sendln(message);
    }

    /**
     * Вещает сообщение всем подключенным пользователям, кроме указанного.
     *
     * @param message сообщение для вещания
     * @param excludedClient клиент, которому не следует присылать сообщение
     */
    public void broadcast(String message, Client excludedClient) {
        for (var client : getClients())
            if (client != excludedClient)
                client.sendln(message);
    }

    /**
     * Создает и запускает новый поток для обработки клиента
     * (получение и отправка сообщений).<br>
     * Данный поток живет до тех пор, пока не будет разорвано соединение.
     */
    public void processClient(Client client) {
        new Thread(() -> {
            System.out.printf("Client %s connected\n", client);

            while (client.hasNewMessage()) {
                var line = client.receiveMessage();

                if (line.charAt(0) == '/') {
                    var proc = ClientCommands.processor;
                    proc.execute(
                        line,
                        new ClientCommands.ClientContextData(client, client.user, client.group)
                    );

                    if (proc.getLastError() != null)
                        client.sendln(proc.getLastError());
                    else if (proc.getOutput() != null)
                        client.send(proc.getOutput());
                    continue;
                }

                client.sendMessageToChat(line);
            }

            getClients().remove(client);
            System.out.printf("Client %s disconnected\n", client);
        }).start();
    }

    private void processInput() {
        while (true) {
            if (!in.hasNextLine()) {
                stop();
                return;
            }
            var msg = in.nextLine();

            if (msg.equals("/exit")) {
                stop();
                return;
            }
        }
    }

    public static void main(String[] args) {
        ClientCommands.init();
        new ServerMain().start();
    }
}
