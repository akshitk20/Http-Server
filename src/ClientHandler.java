import handlers.DeleteClientHandler;
import handlers.GetClientHandler;
import handlers.PostClientHandler;
import handlers.PutClientHandler;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                //getClientHandler.serveFile(out, outputStream, filePath);
                getClientHandler.handleGetRequest(requestedFile, out);
            } else if (Constants.POST.equals(method)) {
                PostClientHandler postClientHandler = new PostClientHandler();
                if (requestedFile.contains("upload")) {
                    postClientHandler.uploadFile(reader, out);
                } else if (requestedFile.contains("download")) {
                    postClientHandler.downloadFile(requestedFile, out, outputStream);
                } else {
                    postClientHandler.handlePostRequest(reader, out);
                }
            } else if (Constants.PUT.equals(method)) {
                PutClientHandler putClientHandler = new PutClientHandler();
                putClientHandler.handlePutRequest(reader, out, filePath);
            } else if (Constants.DELETE.equals(method)) {
                DeleteClientHandler deleteClientHandler = new DeleteClientHandler();
                deleteClientHandler.handleDeleteRequest(filePath, out);
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
}
