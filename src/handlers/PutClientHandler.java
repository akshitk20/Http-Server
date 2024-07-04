package handlers;

import org.json.JSONObject;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;

public class PutClientHandler implements RouteHandler {
    public void handlePutRequest(BufferedReader reader, PrintWriter out, Path path,
                                 Map<Integer, JSONObject> items) {
        // handle put handling
        System.out.println("Starting PUT method");
        File file = path.toFile();
        if (path.toFile().toString().contains("/items")) {
            String filePath = path.toFile().toString();
            String[] parts = filePath.split("/");
            if (parts.length == 3 && "items".equals(parts[1])) {
                try {
                    int id = Integer.parseInt(parts[2]);
                    JSONObject item = items.get(id);
                    if (item != null) {
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
                        items.put(id, jsonObject);
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: application/json");
                        out.println();
                        out.println(jsonObject);
                    } else {
                        out.println("HTTP/1.1 404 NOT FOUND");
                        out.println();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // send response to client
                    out.println("HTTP/1.1 500 Internal Server Error");
                    out.println("Content-Type: application/json");
                    out.println();
                    out.println("Failed to update resource.");
                }

            }
        } else {
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
    }

    @Override
    public void handle(String path, String method,
                       BufferedReader in, PrintWriter out,  Map<Integer, JSONObject> items) throws IOException {

    }
}
