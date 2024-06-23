import handlers.GetClientHandler;
import handlers.PostClientHandler;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
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
            String method = tokenizer.nextToken();
            String requestedFile = tokenizer.nextToken().toLowerCase();

            // default file to serve
            if (requestedFile.equals("/")) {
                requestedFile = "/index.html";
            }

            // building output
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
            BufferedOutputStream outputStream = new BufferedOutputStream(clientSocket.getOutputStream());
            Path filePath = Paths.get("public", requestedFile);
            if (Constants.GET.equals(method)) {
                GetClientHandler getClientHandler = new GetClientHandler();
                getClientHandler.serveFile(out, outputStream, filePath);
            } else if (Constants.POST.equals(method)) {
                PostClientHandler postClientHandler = new PostClientHandler();
                postClientHandler.handlePostRequest(reader, out);
            } else if (Constants.PUT.equals(method)) {
                handlePutRequest(reader, out, filePath);
            } else if (Constants.DELETE.equals(method)) {
                handleDeleteRequest(filePath, out);
            } else {
                // File not found
                // send response to client
                out.println("HTTP/1.1 405 Method not allowed");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<h1>405 Method not allowed</h1>");
                out.flush();
            }
            System.out.println("Handled request: " + input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePutRequest(BufferedReader reader, PrintWriter out, Path path) {
        // handle put handling
        System.out.println("Starting PUT method");
        File file = path.toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Read and discard the request headers
            String line;
            int contentLength = 0;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Content-Length")) {
                    System.out.println("ignoring this " + line);
                    contentLength = Integer.parseInt(line.split(": ")[1]);
                }
            }
            // Read the request body
            char[] bodyChars = new char[contentLength];
            reader.read(bodyChars, 0, contentLength);
            String requestBody = new String(bodyChars);
            writer.write(requestBody);

            // send response to client
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("Resource updated successfully.");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // send response to client
            out.println("HTTP/1.1 500 Internal Server Error");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("Failed to update resource.");
        }
    }
    private void handleDeleteRequest(Path path, PrintWriter out) {
        // handle delete request
        File file = path.toFile();
        if (file.exists()) {
            if (file.delete()) {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("File deleted successfully");
            } else {
                out.println("HTTP/1.1 500 Internal Server Error");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("Failed to delete the file");
            }
        } else {
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("File not found");
        }
    }
}
