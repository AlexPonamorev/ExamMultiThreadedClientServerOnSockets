import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

public class Writer implements Runnable, Close {

    private final Socket clientSocket;
    private final ConcurrentHashMap<Integer, Socket> clientSocketByClientPor;
    private final SynchronousQueue<SimpleMessage> queueOfMessages;


    public Writer(Socket socket, SynchronousQueue<SimpleMessage> queue, ConcurrentHashMap<Integer, Socket> clientSocketByClientPor) {
        this.clientSocket = socket;
        this.queueOfMessages = queue;
        this.clientSocketByClientPor = clientSocketByClientPor;
    }

    /**
     * инструкции для потока отправляют сообщение из очереди
     * каждого потока
     * всем клиентам кроме отправившего
     */
    @Override
    public void run() {
        while (!Thread.currentThread().interrupted()) {
            try {
                // блокируемся пока в очереди не появится сообщение
                SimpleMessage message = queueOfMessages.take();

                // Проверяем на слово Exit и на
                // факт одинаковых соединений, чтобы не отправлять сообщение
                // клиенту отправившего данное сообщение
                if (closeConnect(message)) {  // удалить сообщение и id соединения из коллекции
                    queueOfMessages.remove(message);
                    clientSocketByClientPor.remove(clientSocket.getPort());
                    System.out.println("----------------------------------------------------------" +
                            "\n" + "there was a shutdown: " + clientSocket +
                            "\n" + "Total connections: " + clientSocketByClientPor.size() +
                            "\n");
                } else {
                    for (Socket fromMap : clientSocketByClientPor.values()) {
                        if (!((clientSocket.getPort()) == (fromMap.getPort()))) {
                            ObjectOutputStream out = new ObjectOutputStream(fromMap.getOutputStream());
                            out.writeObject(message);
                            out.flush();
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
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
