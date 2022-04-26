import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

public class Server {
    static ExecutorService executorService = Executors.newFixedThreadPool(7);
    private int countConnections;
    private final ConcurrentHashMap<Integer, Socket> clientSocketByClientPort;
    private SynchronousQueue<SimpleMessage> queueMessages;
    private Socket serverSocket;

    public Server() {
        clientSocketByClientPort = new ConcurrentHashMap<>();

    }

    private void printAboutConnection(Socket socket) {
        System.out.println("----------------------------------------------------------" +
                "\n" + "Произошло подключение, создан сокет для общения с клиентом : " + socket.toString() +
                "\n" + "Количество соединений на данный момент : " + clientSocketByClientPort.size() +
                "\n");
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(3348)) {
            System.out.println("Server socket created, command console reader for listen to server commands : ");

            // стартуем цикл при условии что серверный сокет не закрыт
            while (!serverSocket.isClosed()) {

                // слушаем серверный сокет на предмет запроса на подключение - цикл ждет здесь
                // при подключении создается сокет для общения с клиентом
                Socket clientSocket = serverSocket.accept();
                clientSocketByClientPort.put(clientSocket.getPort(), clientSocket);

                printAboutConnection(clientSocket);
                queueMessages = new SynchronousQueue<>();
                // после получения запроса на подключение сервер создаёт сокет для общения с данным клиентом

                // далее для этого клиента создается нить(new ThreadCommunicationClient) с двумя нитями внутри - чтения и записи
                // в данную нить передаем сокет подключившегося клиента

                // нить выделяется пулом потоков
                executorService.execute(new ThreadCommunicationClient(clientSocket, queueMessages, clientSocketByClientPort));
                System.out.println("Connection accepted...");

            }
            // закрыть пул нитей после завершения работы всех нитей
            executorService.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();

    }
}
