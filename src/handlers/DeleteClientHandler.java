package handlers;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class DeleteClientHandler implements RouteHandler {
    @Override
    public void handle(String path, String method,
                       BufferedReader in, PrintWriter out,  Map<Integer, JSONObject> items) throws IOException {
        File file = new File("public/" + path);
        if (path.contains("items")) {
            String[] parts = path.split("/");
            if (parts.length == 3 && "items".equals(parts[1])) {
                int id = Integer.parseInt(parts[2]);
                if (items.containsKey(id)) {
                    items.remove(id);
                    out.println("HTTP/1.1 204 No Content");
                    out.println();
                } else {
                    out.println("HTTP/1.1 404 Not Found");
                    out.println("Content-Type: application/json");
                    out.println();
                    out.println("Invalid id");
                }
            }
        } else {
            if (file.exists()) {
                if (file.delete()) {
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/plain");
                    out.println();
                    out.println("File deleted successfully");
                } else {
                    out.println("HTTP/1.1 500 Internal Server Error");
                    out.println("Content-Type: text/plain");
                    out.println();
                    out.println("Failed to delete the file");
                }
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("File not found");
            }
        }
    }
}
