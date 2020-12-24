public interface Close {
    final static String SHUTDOWN = "/exit";


    public boolean closeConnect(SimpleMessage message);
}
