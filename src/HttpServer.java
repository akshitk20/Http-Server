import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    private final int port;

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() {
        ClientHandler.initializeRoutes();
        try(ServerSocket socket = new ServerSocket(port)) {
            System.out.println("Listening for connection on port " + port);
            while (true) {
                Socket client = socket.accept();
                new Thread(new ClientHandler(client)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
