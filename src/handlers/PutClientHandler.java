package handlers;

import org.json.JSONObject;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PutClientHandler implements RouteHandler {
    private static final Logger logger = Logger.getLogger(PutClientHandler.class.getName());

    @Override
    public void handle(String path, String method,
                       BufferedReader reader, PrintWriter out,
                       Connection connection) throws IOException {
        // handle put handling
        logger.info("Starting PUT method");
        File file = new File("public/" + path);
        if (path.contains("/items")) {
            logger.info("starting PUT /items endpoint");
            String[] parts = path.split("/");
            if (parts.length == 3 && "items".equals(parts[1])) {
                try {
                    int id = Integer.parseInt(parts[2]);
                    int contentLength = 0;
                    String line;
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
                    System.out.println("PUT body is " + requestBody);
                    JSONObject jsonObject = new JSONObject(requestBody);
                    String sql = "update item set name = ?, description = ? where id = ?";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, jsonObject.getString("name"));
                    statement.setString(2, jsonObject.getString("description"));
                    statement.setInt(3, id);
                    int rows = statement.executeUpdate();
                    if (rows > 0) {
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: application/json");
                        out.println();
                        out.println(jsonObject);
                        logger.info("PUT completed for id " + id);
                    } else {
                        logger.info("PUT not completed due to invalid id");
                        out.println("HTTP/1.1 404 NOT FOUND");
                        out.println();
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    // send response to client
                    out.println("HTTP/1.1 500 Internal Server Error");
                    out.println("Content-Type: application/json");
                    out.println();
                    out.println("Failed to update resource.");
                }

            }
        } else {
            logger.info("starting file content update endpoint");
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
                logger.info("completed file updation");
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                // send response to client
                out.println("HTTP/1.1 500 Internal Server Error");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("Failed to update resource.");
            }
        }
    }
}
