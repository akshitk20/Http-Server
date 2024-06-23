import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class GetClientHandler {

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

}
