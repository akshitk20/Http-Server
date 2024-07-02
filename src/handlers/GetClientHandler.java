package handlers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

import org.json.JSONObject;

public class GetClientHandler {
    // simple get request method that serves the index.html file
    public void serveFile(PrintWriter out, BufferedOutputStream outputStream, Path filePath) throws IOException {
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

    public void handleGetRequest(String filePath, PrintWriter out, Map<Integer, JSONObject> items) {

        if ("/index.html".equals(filePath)) {
            // updated GET request method to render dynamic html content
            renderTemplate("templates/index.html" , out);
        } else if (filePath.contains("items")) {
            // GET the response from map and return JSON response
            String[] parts = filePath.split("/");
            if (parts.length == 3 && "items".equals(parts[1])) {
                int id = Integer.parseInt(parts[2]);
                JSONObject item = items.get(id);
                if (item != null) {
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: application/json");
                    out.println();
                    out.println(item);
                } else {
                    out.println("HTTP/1.1 404 NOT FOUND");
                    out.println();
                }
            }
        } else {
            File file = new File("public" + filePath);
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
    }

    private void renderTemplate(String templatePath,  PrintWriter out) {
        System.out.println("starting enhanced get method");
        try (BufferedReader reader  = new BufferedReader(new FileReader(templatePath))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            StringBuilder dynamicContent = new StringBuilder("<h1> Welcome to Dynamic HTTP Server </h1>");
            dynamicContent.append("\n");
            dynamicContent.append("\t\t").append("<p>This is the home page.</p>").append("\n");
            dynamicContent.append("\t\t").append("<p>Current Time: ").append(LocalDateTime.now()).append("</p>");
            dynamicContent.append("\n");
            dynamicContent.append("\t\t").append("<p> Requested Path: ").append(templatePath).append("</p>");
            dynamicContent.append("\n");
            dynamicContent.append("\t\t").append("<a href=\"form.html\">Go to Form</a>");
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
