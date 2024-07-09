package handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteClientHandler implements RouteHandler {
    private static final Logger logger = Logger.getLogger(DeleteClientHandler.class.getName());

    @Override
    public void handle(String path, String method,
                       BufferedReader in, PrintWriter out, Connection connection) throws IOException {
        logger.info("Handling DELETE request");
        File file = new File("public/" + path);
        if (path.contains("items")) {
            logger.info("Handling DELETE /items request");
            String[] parts = path.split("/");
            if (parts.length == 3 && "items".equals(parts[1])) {
                int id = Integer.parseInt(parts[2]);
                String sql = "delete from item where id = ?";
                try {
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setInt(1, id);
                    int rows = statement.executeUpdate();
                    if (rows > 0) {
                        out.println("HTTP/1.1 204 No Content");
                        out.println();
                        logger.info("Delete completed for id " + id);
                    } else {
                        out.println("HTTP/1.1 404 Not Found");
                        out.println("Content-Type: application/json");
                        out.println();
                        out.println("Invalid id");
                        logger.info("Delete not completed");
                    }
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        } else {
            logger.info("starting DELETE for file endpoint");
            if (file.exists()) {
                if (file.delete()) {
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/plain");
                    out.println();
                    out.println("File deleted successfully");
                    logger.info("File deleted successfully");
                } else {
                    out.println("HTTP/1.1 500 Internal Server Error");
                    out.println("Content-Type: text/plain");
                    out.println();
                    out.println("Failed to delete the file");
                    logger.info("Failed to delete the file");
                }
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("File not found");
                logger.info("File not found " + file.getName());
            }
        }
    }
}
