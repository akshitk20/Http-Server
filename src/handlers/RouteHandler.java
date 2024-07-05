package handlers;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public interface RouteHandler {
    void handle(String path, String method, BufferedReader in, PrintWriter out,  Map<Integer, JSONObject> items) throws IOException;
}
