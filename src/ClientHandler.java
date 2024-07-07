import config.AuthUtils;
import config.DatabaseConnection;
import handlers.*;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private static final Map<Integer, JSONObject> items = new HashMap<>();
    private static final Map<String, Map<String, RouteHandler>> routes = new HashMap<>();

    private final DatabaseConnection databaseConnection;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.databaseConnection = new DatabaseConnection();
    }

    @Override
    public void run() {
        try (InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream())) {
            // reading request
            BufferedReader reader = new BufferedReader(isr);
            String input = reader.readLine();
            if (null == input || input.isEmpty()) {
                return;
            }

            System.out.println("Request received " + input);
            // parse input
            String[] requestParts = input.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];

            // Read headers
            String authHeaders = null;
            while (!(input = reader.readLine()).isEmpty()) {
                if (input.startsWith("Authorization: ")) {
                    authHeaders = input.substring("Authorization".length() + 1).trim();
                    break;
                }
            }

            // handle the request based method and path
            RouteHandler routeHandler = findRouteHandler(method, path);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
            // get connection from database
            Connection connection = databaseConnection.getConnection();

            // Check for basic authentication
            if (!isAuthenticated(authHeaders)) {
                sendUnAuthorizedResponse(out);
                return;
            }

            if (routeHandler != null) {
                routeHandler.handle(path, method, reader, out, connection);
            } else if (path.contains("download")) {
                BufferedOutputStream outputStream = new BufferedOutputStream(clientSocket.getOutputStream());
                PostClientHandler postClientHandler = new PostClientHandler();
                postClientHandler.downloadFile(path, out, outputStream);
            } else {
                sendNotSupportedMethod(out);
            }
            System.out.println("Handled request: " + input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initializeRoutes() {
        // Define Routes
        addRoute("GET", "/", new GetClientHandler());
        addRoute("GET", "/items/{id}", new GetClientHandler());
        addRoute("GET","/form.html", new GetClientHandler());
        addRoute("POST", "/submit", new PostClientHandler());
        addRoute("POST", "/items", new PostClientHandler());
        addRoute("POST", "/upload", new PostClientHandler());
        //addRoute("POST", "/download?filename=test.txt", new PostClientHandler());
        addRoute("PUT", "/testfile.txt", new PutClientHandler());
        addRoute("PUT","/items/{id}", new PutClientHandler());
        addRoute("DELETE","/testfile.txt", new DeleteClientHandler());
        addRoute("DELETE","/items/{id}", new DeleteClientHandler());
    }

    private static void addRoute(String method, String path, RouteHandler handler) {
        routes.putIfAbsent(path, new HashMap<>());
        routes.get(path).put(method, handler);
    }

    private void sendNotSupportedMethod(PrintWriter out) {
        // send response to client
        out.println("HTTP/1.1 405 Method not allowed");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<h1>405 Method not allowed</h1>");
        out.flush();
    }

    private RouteHandler findRouteHandler(String method, String path) {
        // check for exact match
        if (routes.containsKey(path) && routes.get(path).containsKey(method)) {
            return routes.get(path).get(method);
        }

        // check for dynamic routes
        for (Map.Entry<String, Map<String, RouteHandler>> entry : routes.entrySet()) {
            String routePattern = entry.getKey();
            if (routePattern.contains("{")) {
                String regex = routePattern.replace("{id}","(\\w+)");
                if (path.matches(regex)) {
                    return entry.getValue().get(method);
                }
            }
        }

        return null;
    }

    private void sendUnAuthorizedResponse(PrintWriter out) {
        out.println("HTTP/1.1 401 UNAUTHORIZED");
        out.println("WWW-Authenticate: Basic realm=\"Restricted\"");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<h1>401 Unauthorized</h1>");
    }

    private boolean isAuthenticated(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return false;
        }

        String encodedCredentials = authHeader.substring("Basic ".length()).trim();
        return AuthUtils.isValidCredentials(encodedCredentials);
    }
}
