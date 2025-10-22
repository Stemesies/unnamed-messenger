public class Server {

    SimpleServerSocket socket = null;

    public void start() {
        socket = new SimpleServerSocket(8080);
        if(socket.isClosed()) {
            socket = null;
            return;
        }

        System.out.println("Server started");

        while(true) {
            var client = socket.accept();

            System.out.println("Client connected");

            while(client.hasNewMessage()) {
                String line = client.receiveMessage();
                System.out.println(line);
                client.sendMessage("server: " + line);
            }

            System.out.println("Client disconnected");
        }
    }

    public static void main(String[] args) {
        var client = new Server();
        client.start();
    }


}
