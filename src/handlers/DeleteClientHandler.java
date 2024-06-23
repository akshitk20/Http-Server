package handlers;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;

public class DeleteClientHandler {
    public void handleDeleteRequest(Path path, PrintWriter out) {
        // handle delete request
        File file = path.toFile();
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
