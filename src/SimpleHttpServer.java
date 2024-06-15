import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class SimpleHttpServer {
    private int port;
    public SimpleHttpServer(int port) {
        this.port = port;
    }
    public void start()  {

        try(final ServerSocket server = new ServerSocket(port)) {
            System.out.println("Listening for connection on port " + port);
            while (true) {
                try(final Socket client = server.accept()) {
                    // 1. Read HTTP request from the client socket
                    InputStreamReader isr = new InputStreamReader(client.getInputStream());
                    BufferedReader reader = new BufferedReader(isr);
                    String line = reader.readLine();
                    while (!line.isEmpty()) {
                        System.out.println(line);
                        line = reader.readLine();
                    }

                    // 2. Prepare an HTTP response
                    LocalDateTime date = LocalDateTime.now();
                    String response = "HTTP/1.1 200 OK \r\n\r\n" + date;
                    System.out.println(response);

                    // 3. Send HTTP response to the client
                    client.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
                    client.getOutputStream().flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }
}