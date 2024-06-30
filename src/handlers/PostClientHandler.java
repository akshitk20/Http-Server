package handlers;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PostClientHandler {

    public void handlePostRequest(BufferedReader reader, PrintWriter out) throws IOException {
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
                out.println("Content-Disposition: attachment; filename=\"" + requestedFile + "\"");
                out.println("Content-Length: " + fileBytes.length);
                out.println();
                out.flush();
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
}
