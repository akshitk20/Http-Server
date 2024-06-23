package handlers;

import java.io.*;
import java.nio.file.Path;

public class PutClientHandler {
    public void handlePutRequest(BufferedReader reader, PrintWriter out, Path path) {
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
}
