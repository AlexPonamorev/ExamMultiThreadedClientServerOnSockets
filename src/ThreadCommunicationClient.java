import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * Обьект данной нити создается под каждого подключившегося клиента
 * в свою очередь создаёт две нити для приема и отправки сообщений
 * <p>
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

    private Socket clientSocket;

    private ConcurrentHashMap<Integer, Socket> connections;
    private SynchronousQueue<SimpleMessage> queueOfMessages;

    public ThreadCommunicationClient(Socket clientSocket, SynchronousQueue<SimpleMessage> queueOfMessages,
                                     ConcurrentHashMap<Integer, Socket> connections) {
        this.clientSocket = clientSocket;
        this.queueOfMessages = queueOfMessages;
        this.connections = connections;


    }

    /**
     * инструкции нитей записи и чтения  для работы с клиентом
     */
    @Override
    public void run() {

        new Thread(new Writer(clientSocket,
                queueOfMessages, connections)).start();

        new Thread(new Reader(clientSocket,
                queueOfMessages,
                connections)).start();
    }
}