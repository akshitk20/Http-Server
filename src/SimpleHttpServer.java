import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class SimpleHttpServer {
    public static void main(String[] args)  {

        try(final ServerSocket server = new ServerSocket(8080)) {
            System.out.println("Listening for connection on port 8080");
            while (true) {
                final Socket client = server.accept();
                // 1. Read HTTP request from the client socket
                InputStreamReader isr = new InputStreamReader(client.getInputStream());
                BufferedReader reader = new BufferedReader(isr);
                String line = reader.readLine();
                while (!line.isEmpty()) {
                    System.out.println(line);
                    line = reader.readLine();
                }
                // 2. Prepare an HTTP response
                // 3. Send HTTP response to the client
                // 4. Close the socket

            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }
}