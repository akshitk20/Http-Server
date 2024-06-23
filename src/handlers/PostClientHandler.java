package handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
