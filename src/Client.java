import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.Properties;

public class Client {
    private String name;
    private BufferedReader reader;
    private Properties connectionProperties;
    private Socket socket;

    public Client(String name, Properties connectionProperties) {
        Objects.requireNonNull(connectionProperties, "connectionProperties is not be null");
        this.connectionProperties = connectionProperties;
        setName(name);
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    private void setName(String name) {
        Objects.requireNonNull(name, "name is not be null");
        if (name.length() > 10)
            throw new IllegalArgumentException("name is not be > 10 characters");
        this.name = name.trim();
    }

    /**
     * Подключаемся к серверу
     *
     * счититываем адрес
     * создаем сокет клиента
     */
    private void connectToServer() {
        try(FileInputStream fileInputStream = new FileInputStream("/home/alex/Document/IdeaProjects27May/IdeaProjects/Server_OfMine/resources/connection.properties")){
            connectionProperties.load(fileInputStream);
            this.socket = new Socket(
                    connectionProperties.getProperty("ip"),
                    Integer.parseInt(connectionProperties.getProperty("port")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Внутри создается поток который, читает с командной строки
     * и отправляет эти данные на сервер, поток зациклен
     * поэтому снова можно вводить данные при каждой отправке.
     * Выходим из потока и рвем соединение если введено слово /exit
     */
    private void readMessageFromCommandLineAndSend() {
        new Thread(() -> {

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println("Enter you Message ... ");
                    SimpleMessage message = new SimpleMessage(reader.readLine(), name);
                    ObjectOutputStream out =  new ObjectOutputStream(socket.getOutputStream());

                    /**
                     * objectOutputStream objectOutput = new ObjectOutputStream(fileStream)){
                     используя поток записи записывает object в файл в виде .bin
                     objectOutput.writeObject(object); // запись из потока fileStream*/
                    // сериализация
                    out.writeObject(message);
                    // отправка
                    out.flush();
                    IsClose(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void IsClose(SimpleMessage message) throws Exception {
        if (message.getText().equalsIgnoreCase("$exit")){
            Thread.currentThread().interrupt();
            socket.close();
        }
    }

    /**
     * Внутри создается поток который читает данные с сервера
     * и выводит их в консоль
     */
    void readAndPrintMessageFromServer() {
        new Thread(() -> {
            // пока флаг isInterrupted - true
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                    // десериализация
                    SimpleMessage simpleMessage = (SimpleMessage) objectInputStream.readObject();
                    System.out.println("Message from server -> " + simpleMessage);
                } catch (IOException | ClassNotFoundException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }


    private void start() throws InterruptedException {
        connectToServer();
        readMessageFromCommandLineAndSend();
        readAndPrintMessageFromServer();

    }


    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("Enter you name : ");
            String name = reader.readLine();
            Client client = new Client(name, new Properties());

            client.start();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
