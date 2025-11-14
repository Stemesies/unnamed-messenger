package network;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Небольшой класс-обертка серверного сокета.
 *
 * <p>После окончания работы с этим классом должен
 * быть вызван метод {@link #close()}
 */
public class SimpleServerSocket implements Closeable {
    ServerSocket serverSocket;

    private boolean isClosed = false;

    public  SimpleServerSocket(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Error creating server socket: " + e.getMessage());
            close();
        }
    }

    /**
     * Устанавливает соединение с пытающимся подключиться клиентом.<br>
     * Возможно блокирование потока, пока нет поступающих "запросов на соединение". <br>
     *
     * @return готовая к работе {@link SimpleSocket} обертка подключенного клиента.
     */
    public SimpleSocket accept() {
        try {
            return new SimpleSocket(serverSocket.accept());
        } catch (IOException e) {
            System.err.println("Error accepting client: " + e.getMessage());
            return null;
        }
    }

    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() {
        isClosed = true;
        try {
            serverSocket.close();
        } catch (IOException ignored) {}
        serverSocket = null;
    }
}
