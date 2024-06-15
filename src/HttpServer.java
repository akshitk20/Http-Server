import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public class HttpServer {
    private int port;

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() {
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
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream())) {
                // reading request
                BufferedReader reader = new BufferedReader(isr);
                String input = reader.readLine();
                if (null == input || input.isEmpty()) {
                    return;
                }
                StringTokenizer tokenizer = new StringTokenizer(input);
                String method = tokenizer.nextToken().toLowerCase();
                String requestedFile = tokenizer.nextToken().toLowerCase();

                // default file to serve
                if (requestedFile.equals("/")) {
                    requestedFile = "/index.html";
                }

                // building output
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
                BufferedOutputStream outputStream = new BufferedOutputStream(clientSocket.getOutputStream());
                Path filePath = Paths.get("public", requestedFile);
                if (Files.exists(filePath)) {
                    byte[] fileData = Files.readAllBytes(filePath);
                    String contentType = Files.probeContentType(filePath);
                    // send HTTP response
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: " + contentType);
                    out.println("Content-Length: " + fileData.length);
                    out.println();
                    out.flush();
                    outputStream.write(fileData, 0, fileData.length);
                    outputStream.flush();
                } else {

                    // File not found
                    out.println("HTTP/1.1 404 Not Found");
                    out.println("Content-Type: text/html");
                    out.println();
                    out.println("<h1>404 Not Found</h1>");
                    out.flush();
                }
                System.out.println("Handled request: " + input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
