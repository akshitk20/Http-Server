public class Main {
    public static void main(String[] args) {
        //SimpleHttpServer server = new SimpleHttpServer(8080);
        //server.start();
        HttpServer server = new HttpServer(8080);
        server.start();
    }
}
