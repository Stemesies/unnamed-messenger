import java.io.*;
import java.net.Socket;

/**
 * Небольшой класс-обертка обычного сокета,
 * включающий в себя работу с отправкой и получением
 * информации, а также отлов исключений.
 * <p>
 * После окончания работы с этим классом должен
 * быть вызван метод {@link #close()}
 */
public class SimpleSocket implements Closeable {
    private Socket socket;

    private PrintWriter out = null;
    private BufferedReader in = null;

    private String peekMessage = null;

    private boolean isClosed = false;



    public SimpleSocket(Socket socket) {
        this.socket = socket;
        loadSocket();

    }

    public SimpleSocket(String host, int port) {
        try {
            socket = new Socket(host, port);
            loadSocket();
        } catch (IOException e) {
            System.err.println("Error opening socket: " + e.getMessage());
            close();
        }
    }

    private void loadSocket() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Error opening socket streams: " + e.getMessage());
            close();

        }
    }

    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Отправляет сообщение данному сокету
     * @param message сообщение
     * @throws IllegalStateException если сокет закрыт
     */
    public void sendMessage(String message) throws IllegalStateException {
        if (isClosed)
            throw new IllegalStateException("Socket is closed");

        out.println(message);
    }

    public boolean hasNewMessage() {
        if(peekMessage != null)
            return true;
        if(isClosed)
            return false;

        peekMessage = rawGetMessage();

        return peekMessage != null;
    }

    /**
     * Возвращает сообщение от сокета.
     * <p>
     * Данный метод блокирует дальнейшее выполнение кода,
     * пока не будет получен ответ или не будет выкинуто исключение.
     * <p>
     *
     * @return message, если сообщение успешно получено<br>
     *         null, если клиент был отключен
     * @throws IllegalStateException если сокет закрыт
     */
    public String receiveMessage() throws IllegalStateException {
        if(peekMessage != null) {
            var msg = peekMessage;
            peekMessage = null;
            return msg;
        }
        if (isClosed)
            throw new IllegalStateException("Socket is closed");

        return rawGetMessage();
    }

    private String rawGetMessage() {
        try {
            return in.readLine();
        } catch (IOException e) {
            close();
            return null;
        }
    }

    @Override
    public void close() {
        isClosed = true;
        if(socket == null)
            return;

        try {
            socket.close();
        } catch (IOException ignored) {}
        out.close();
        try {
            in.close();
        } catch (IOException ignored) {}

        socket = null;
        out = null;
        in = null;
    }
}
