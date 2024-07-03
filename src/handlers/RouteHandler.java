package handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public interface RouteHandler {
    void handle(String path, String method, BufferedReader in, PrintWriter out) throws IOException;
}
