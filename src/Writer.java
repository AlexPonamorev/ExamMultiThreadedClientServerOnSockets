import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

public class Writer
        implements Runnable, Close {

    private Socket clientSocket;
    private ConcurrentHashMap<Integer, Socket> connections;
    private SynchronousQueue<SimpleMessage> queueOfMessages;


    public Writer(Socket socket, SynchronousQueue<SimpleMessage> queue, ConcurrentHashMap<Integer, Socket> connections) {
        this.clientSocket = socket;
        this.queueOfMessages = queue;
        this.connections = connections;
    }

    /**
     * Инструкции для рассылки сообщений всем клиентам кроме отправившего сообщение
     * */
    @Override
    public void run() {
        while (!Thread.currentThread().interrupted()) {
            try {
                // блокируемся пока в очереди не появится сообщение
                SimpleMessage message = queueOfMessages.take();

                // Проверяем на слово Exit и на
                // факт одинаковых соединений, чтобы не отправлять сообщение
                // самому себе
                if (closeConnect(message))
                {  // удалить сообщение из очереди сообщений и
                   // удалить по номеру порта(ключу) соединение(сокет клиента)
                    queueOfMessages.remove(message);
                    connections.remove(clientSocket.getPort());
                    System.out.println("----------------------------------------------------------"+
                            "\n" + "there was a shutdown: " + clientSocket +
                            "\n" + "Total connections: " + connections.size() +
                            "\n");
                }else {
                        // проитерироваться по всем ключам и отправить всем клиентам кроме оправившего
                    for (Socket fromMap : connections.values()) {
                        if (! ( (clientSocket.getPort())==(fromMap.getPort())) ){
                            ObjectOutputStream out = new ObjectOutputStream(fromMap.getOutputStream()) ;
                            out.writeObject(message);
                            out.flush();
                        }
                    }
                }
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
            } catch ( IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean closeConnect(SimpleMessage message) {
        if (message.getText().equalsIgnoreCase(Close.SHUTDOWN)){
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
