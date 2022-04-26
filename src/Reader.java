import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * класс реализующий поток чтения из сокета для каждого клиента
 */
public class Reader implements Runnable, Close {

    private final Socket clientSocket;
    private final SynchronousQueue<SimpleMessage> queue;
    private final ConcurrentHashMap<Integer, Socket> clientSocketByClientPor;


    public Reader(Socket socket, SynchronousQueue<SimpleMessage> queue, ConcurrentHashMap<Integer, Socket> clientSocketByClientPor) {
        this.clientSocket = socket;
        this.queue = queue;
        this.clientSocketByClientPor = clientSocketByClientPor;
    }

    /**
     * нить читает из соединения,
     * кладет сообщение в очередь
     * проверяет флаг завершения
     */
    @Override
    public void run() {
        // проверка флага isInrerrupred о необходимости завершения потока
        // задача потока выполняется пока флаг true
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // дисериализация из потока и приведение к message
                ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

                SimpleMessage simpleMessage = (SimpleMessage) objectInputStream.readObject();
                System.out.println("A message from the client -> " + simpleMessage);

                queue.put(simpleMessage);

                // проверяем сообщение на выход
                closeConnect(simpleMessage);

            } catch (InterruptedException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean closeConnect(SimpleMessage message) {
        if (message.getText().equalsIgnoreCase(Close.SHUTDOWN)) {
            Thread.currentThread().interrupt();
            try {
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

}
