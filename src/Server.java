import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

public class Server {
    static ExecutorService executeIt = Executors.newFixedThreadPool(7);
    private int countConnections;
    private ConcurrentHashMap<Integer, Socket> connections;
    private SynchronousQueue<SimpleMessage> queueMessages;
    private Socket serverSocket;

    public Server() {
        connections = new ConcurrentHashMap<>();

    }

    private void printAboutConnection(Socket socket) {
        System.out.println("----------------------------------------------------------" +
                "\n" + "There was a connection: " + socket.toString() +
                "\n" + "Total connections: " + connections.size() +
                "\n");
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(3348)) {
            System.out.println("Server socket created, command console reader for listen to server commands : ");

            // стартуем цикл при условии что серверный сокет не закрыт
            while (!serverSocket.isClosed()) {

                // слушаем серверный сокет на предмет запроса на подключение - программа блокируется в этой итерации на методе .accept
                // при подключении .accept возвращает сокет для общения с клиентом на определенном порту (открытым клиентским приложением)
                Socket clientSocket = serverSocket.accept();
                connections.put(clientSocket.getPort(), clientSocket);

                printAboutConnection(clientSocket);
                queueMessages = new SynchronousQueue<>();
                // после получения запроса на подключение сервер создаёт сокет для общения с данным клиентом
                
                // для общения с каждым вновь подключившимся клиентом создается объект новой нити
                // который в свою очередь имеет два потока: чтения и записи
                // передаем данный объект в  управление ExecutorService 

                executeIt.execute(new ThreadCommunicationClient(clientSocket,queueMessages,connections));
                System.out.println("Connection accepted...");

            }
            // закрыть пул нитей после завершения работы всех нитей
            executeIt.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();

    }
}
