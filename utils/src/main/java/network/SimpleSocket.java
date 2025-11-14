package network;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Небольшой класс-обертка обычного сокета,
 * включающий в себя работу с отправкой и получением
 * информации, а также отлов исключений.
 *
 * <p>После окончания работы с этим классом должен
 * быть вызван метод {@link #close()}
 */
public class SimpleSocket implements Closeable {
    private Socket socket;
    private boolean isClosed = false;

    private PrintWriter out = null;
    private BufferedReader in = null;

    private String peekMessage = null;

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

    private void ensureOpen() throws IllegalStateException {
        if (isClosed)
            throw new IllegalStateException("Socket is closed");
    }

    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Отправляет сообщение данному сокету.
     *
     * @param message сообщение
     * @throws IllegalStateException если сокет закрыт
     */
    public void sendMessage(String message) throws IllegalStateException {
        ensureOpen();
        out.println(message);
    }

    /**
     * Проверяет, есть ли новые сообщения от сокета.<br>
     * Возможно блокирование потока до тех пор, пока не будет получено
     * новое сообщение, или не будет закрыт сокет. <br>
     * <br>
     * При успешном выполнении метода, успешность выполнения
     * {@link #receiveMessage()} гарантирована и моментальна.
     *
     * @return true, если сообщение есть<br>
     *         false, если сокет закрыт, либо произошла ошибка.
     */
    public boolean hasNewMessage() {
        if (peekMessage != null)
            return true;
        if (isClosed)
            return false;

        peekMessage = rawGetMessage();

        return peekMessage != null;
    }

    /**
     * Возвращает сообщение от сокета.
     *
     * <p>Возможно блокирование потока до тех пор, пока не будет получено
     * новое сообщение или не будет закрыт сокет.
     *
     * <p>Для проверки на новое сообщение можете использовать {@link #hasNewMessage()}
     *
     * @return message, если сообщение успешно получено<br>
     *         null, если клиент был отключен
     * @throws IllegalStateException если сокет закрыт
     */
    public String receiveMessage() throws IllegalStateException {
        if (peekMessage != null) {
            var msg = peekMessage;
            peekMessage = null;
            return msg;
        }

        ensureOpen();
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

    /**
     * Закрывает сокет и все необходимые стримы.<br>
     * После закрытия, экземпляр класса не может быть использован
     * и будет выбрасывать {@link IllegalStateException} при попытках использования.<br>
     * <br>
     * Данный метод безопасен, и может быть вызван, даже если сокет закрыт.
     */
    @Override
    public void close() {
        isClosed = true;

        if (socket == null)
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
