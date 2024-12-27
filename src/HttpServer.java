import config.LoggingConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HttpServer {
    private final int port;
    private final Executor executor = Executors.newFixedThreadPool(8);

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() {
        ClientHandler.initializeRoutes();
        LoggingConfig.configureLogger();
        try(ServerSocket socket = new ServerSocket(port)) {
            System.out.println("Listening for connection on port " + port);
            while (true) {
                Socket client = socket.accept();
                //new Thread(new ClientHandler(client)).start();
                executor.execute(new ClientHandler(client));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
