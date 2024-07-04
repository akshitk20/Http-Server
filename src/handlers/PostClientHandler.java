package handlers;

import org.json.JSONObject;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PostClientHandler implements RouteHandler {

    public void handlePostRequest(String path, BufferedReader reader, PrintWriter out, Map<Integer, JSONObject> items) throws IOException {
        try {
            if (path.contains("items")) {
                // handling post request for /items by sending a json response
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

                JSONObject item = new JSONObject(body);
                int id = items.size() + 1;
                item.put("id", id);
                items.put(id, item);

                out.println("HTTP/1.1 201 created");
                out.println("Content-Type: application/json");
                out.println();
                out.println(item);
            } else {
                // Parsing form data to return the result of a form submitted
                String line;
                int contentLength = 0;
                // Read and discard the request headers
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

                // Send Response to client
                System.out.println("Post payload with body " + formData);
                String responseMessage = "Received POST data " + formData;
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<h1>Form Submission Successful</h1>");
                out.println("<p>" + responseMessage + "</p>");
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("HTTP/1.1 500 Internal Server Error");
            out.println();
            out.println(e.getMessage());
        }
    }


    public void uploadFile(BufferedReader reader, PrintWriter out) {
        try {
            String line;
            String fileName = null;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Content-Disposition")) {
                    fileName = line.split("filename=")[1].replaceAll("\"", "");
                    break;
                }
            }
            if (Objects.nonNull(fileName)) {
                File file = new File("uploads", fileName);
                try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
                    while (!(line = reader.readLine()).startsWith("--")) {
                        outputStream.write(line.getBytes());
                    }
                }
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("File uploaded successfully");
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(String requestedFile, PrintWriter out, BufferedOutputStream outputStream) {
        String fileName = requestedFile.split("filename=")[1];
        File file = new File("uploads", fileName);
        if (file.exists()) {
            try {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/octet-stream");
                out.println("Content-Disposition: attachment; filename=\"" + fileName + "\"");
                out.println("Content-Length: " + fileBytes.length);
                out.println();

                // Flush the header
                out.flush();

                // Write the file content
                outputStream.write(fileBytes);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("File not found");
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

    @Override
    public void handle(String path, String method,
                       BufferedReader in, PrintWriter out,  Map<Integer, JSONObject> items) throws IOException {

    }
}
