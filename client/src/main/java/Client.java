import java.util.Scanner;

public class Client {

    SimpleSocket socket = null;
    Scanner in = new Scanner(System.in);

    public void connect() {
        socket = new SimpleSocket("127.0.0.1", 8080);
        if(socket.isClosed()) {
            socket = null;
            return;
        }

        System.out.println("Connected to the server");

        while(!socket.isClosed()) {
            socket.sendMessage(in.nextLine());
            if(socket.hasNewMessage())
                System.out.println(socket.receiveMessage());
        }
    }

    public static void main(String[] args) {
        var client = new Client();
        client.connect();
    }
}
