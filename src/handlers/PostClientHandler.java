package handlers;

import org.json.JSONObject;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostClientHandler implements RouteHandler {
    private static final Logger logger = Logger.getLogger(PostClientHandler.class.getName());

    @Override
    public void handle(String path, String method,
                       BufferedReader reader, PrintWriter out,
                       Connection connection) throws IOException {
        logger.info("Handling POST request");
        if (path.contains("submit")) {
            logger.info("Handling submit request");
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
            logger.info("Post payload with body " + formData);
            String responseMessage = "Received POST data " + formData;
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<h1>Form Submission Successful</h1>");
            out.println("<p>" + responseMessage + "</p>");
            out.flush();
        } else if (path.contains("items")) {
            logger.info("Handling items request");
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
            String name = item.getString("name");
            String description = item.getString("description");

            String sql = "insert into item(name, description) values (?, ?)";
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, description);
                int rows = preparedStatement.executeUpdate();
                if (rows > 0) {
                    out.println("HTTP/1.1 201 created");
                    out.println("Content-Type: application/json");
                    out.println();
                    out.println(item);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (path.contains("upload")) {
            uploadFile(reader, out);
        } else {
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<h1>404 Path not found</h1>");
            out.flush();
        }
    }

    public void uploadFile(BufferedReader reader, PrintWriter out) {
        logger.info("starting file upload");
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
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        logger.info("file upload completed");
    }

    public void downloadFile(String requestedFile, PrintWriter out, BufferedOutputStream outputStream) {
        logger.info("starting file download");
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
                logger.info("file download completed");
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else {
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("File not found");
            out.flush();
            logger.info("File download failed");
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
