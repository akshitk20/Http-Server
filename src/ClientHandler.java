import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
                serveFile(out, outputStream, filePath);
            } else if (Constants.POST.equals(method) && requestedFile.equals("/submit")) {
                handlePostRequest(reader, out);
            } else if (Constants.PUT.equals(method)) {
                handlePutRequest(reader, out);
            } else if (Constants.DELETE.equals(method)) {
                handleDeleteRequest(reader, out);
            } else {
                // File not found
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

    private void handleDeleteRequest(BufferedReader reader, PrintWriter out) {
        // handle delete handling
    }

    private void handlePutRequest(BufferedReader reader, PrintWriter out) {
        // handle put handling
    }

    private void handlePostRequest(BufferedReader reader, PrintWriter out) throws IOException {
        String line;
        int contentLength = 0;
        while (!(line = reader.readLine()).isEmpty()) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
            }
        }

        // Read the body
        char[] charArray = new char[contentLength];
        reader.read(charArray);
        String body = new String(charArray);
        System.out.println("Post payload " + body);

        // Parse form data
        Map<String, String> formData = parseFormData(body);

        // Send Response
        System.out.println("Post payload with body " + formData);
        String responseMessage = "Received POST data " + formData;
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<h1>Form Submission Successful</h1>");
        out.println("<p>" + responseMessage + "</p>");
        out.flush();
    }

    private void serveFile(PrintWriter out, BufferedOutputStream outputStream, Path filePath) throws IOException {
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
    }

    private Map<String, String> parseFormData(String body) {
        Map<String, String> formData = new HashMap<>();
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                formData.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8), URLDecoder.decode(keyValue[1],
                        StandardCharsets.UTF_8));
            }
        }
        return formData;
    }
}
