import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * Обьект данной нити создается под каждого подключившегося клиента
 * в свою очередь создаёт две нити для приема и отправки сообщений
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
 
    @Override
    public void run() {

        new Thread(new Writer(clientSocket,
                queueOfMessages, connections)).start();

        new Thread(new Reader(clientSocket,
                queueOfMessages,
                connections)).start();
    }
}
