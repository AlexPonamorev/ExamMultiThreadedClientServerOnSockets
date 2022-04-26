import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * инструкции обработки для нити общения с клиентом
 * **************************************
 * принимает сокет для общения с данным клиентом
 * имеет два потока для отправки и принятия сообщений
 * <p>
 * общение происходит через сокет
 * сокет - абстракция туннеля в котором два канала
 * канал в котором есть поток чтения InputStream
 * канал в котором есть поток записи OutputStream
 */

public class ThreadCommunicationClient implements Runnable {
    private final Socket clientSocket;
    private final ConcurrentHashMap<Integer, Socket> clientSocketByClientPor;
    private final SynchronousQueue<SimpleMessage> queueOfMessages;

    public ThreadCommunicationClient(Socket clientSocket, SynchronousQueue<SimpleMessage> queueOfMessages,
                                     ConcurrentHashMap<Integer, Socket> clientSocketByClientPor) {
        this.clientSocket = clientSocket;
        this.queueOfMessages = queueOfMessages;
        this.clientSocketByClientPor = clientSocketByClientPor;


    }

    /**
     * инструкции нитей записи и чтения для работы с клиентом
     */
    @Override
    public void run() {

        new Thread(new Writer(clientSocket,
                queueOfMessages, clientSocketByClientPor)).start();

        new Thread(new Reader(clientSocket,
                queueOfMessages,
                clientSocketByClientPor)).start();
    }
}