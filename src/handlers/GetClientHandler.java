package handlers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetClientHandler implements RouteHandler {
    private static final Logger logger = Logger.getLogger(GetClientHandler.class.getName());

    // simple get request method that serves the index.html file
    public void serveFile(PrintWriter out, BufferedOutputStream outputStream, Path filePath) throws IOException {
        logger.info("Handling GET request");
        if (Files.exists(filePath)) {
            byte[] fileData = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            // send HTTP response to client
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + contentType);
            out.println("Content-Length: " + fileData.length);
            out.println();
            out.flush();
            outputStream.write(fileData, 0, fileData.length);
            outputStream.flush();
        } else {
            // File not found
            // send response to client
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<h1>404 Not Found</h1>");
            out.flush();
        }
    }

    @Override
    public void handle(String path, String method, BufferedReader in,
                       PrintWriter out, Connection connection) throws IOException {
        logger.info("Handling GET request");
        if ("/".equals(path)) {
            // updated GET request method to render dynamic html content
            renderTemplate("templates/index.html" , out);
        } else if (path.contains("items")) {
            // GET the response from map and return JSON response
            String[] parts = path.split("/");
            if (parts.length == 3 && "items".equals(parts[1])) {
                int id = Integer.parseInt(parts[2]);
                String sql = "select * from item where id = ?";
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setInt(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        String name = resultSet.getString("name");
                        String description = resultSet.getString("description");

                        // construct json response
                        StringBuilder jsonResponse = new StringBuilder();
                        jsonResponse.append("{");
                        jsonResponse.append("\"id\": ").append(id).append(",");
                        jsonResponse.append("\"name\": \"").append(name).append("\",");
                        jsonResponse.append("\"description\": ").append(description);
                        jsonResponse.append("}");

                        // send response
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: application/json");
                        out.println("Content-Length: " + jsonResponse.length());
                        out.println();
                        out.println(jsonResponse);
                    } else {
                        out.println("HTTP/1.1 404 NOT FOUND");
                        out.println();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            File file = new File("public" + path);
            if (file.exists() && !file.isDirectory()) {
                try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                    String line;
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: " + getMimeType(file.getName()));
                    out.println();
                    while ((line = fileReader.readLine()) != null) {
                        out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println();
            }
        }
        logger.info("GET request handled successfully");
    }

    private void renderTemplate(String templatePath,  PrintWriter out) {
        logger.info("starting enhanced get method");
        try (BufferedReader reader  = new BufferedReader(new FileReader(templatePath))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            StringBuilder dynamicContent = new StringBuilder("<center><h1> Welcome to Dynamic HTTP Server </h1></center>");
            dynamicContent.append("\n");
            dynamicContent.append("\t\t").append("<center><p>This is the home page.</p></center>").append("\n");
            dynamicContent.append("\t\t").append("<center><p>Current Time: ").append(LocalDateTime.now()).append("</p></center>");
            dynamicContent.append("\n");
            dynamicContent.append("\t\t").append("<center><p> Requested Path: ").append(templatePath).append("</p></center>");
            dynamicContent.append("\n");
            dynamicContent.append("\t\t").append("<center><a href=\"form.html\">Go to Form</a></center>");
            String formattedHtmlContent = content.toString().replace(" {{dynamic_content}}", dynamicContent);

            // Format the HTML content with current time and requested path
            System.out.println("Response send is " +formattedHtmlContent);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + getMimeType(templatePath));
            out.println("Content-Length: " + formattedHtmlContent.length());
            // space between headers and body
            out.println();
            out.println(formattedHtmlContent);
            out.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private String getMimeType(String fileName) {
        if (fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }
}
