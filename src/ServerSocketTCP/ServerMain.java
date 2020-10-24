package ServerSocketTCP;

public class ServerMain {
    public static void main(String[] args) {
        int port = 3000;
        Server server = new Server(port);
        server.start();
    }
}
