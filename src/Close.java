public interface Close {
    String SHUTDOWN = "/exit";

    boolean closeConnect(SimpleMessage message);
}
