import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static final int PORT = 8989;

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"), new File("stop-ru.txt"));
        Gson gson = new Gson();
        System.out.println("Starting server at " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started.");
            while (true) {
                try (
                        Socket socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream())
                ) {
                    String[] words = in.readLine().split("\\P{IsAlphabetic}+");
                    out.println(gson.toJson(engine.multiplySearch(words)));
                }
            }
        } catch (IOException e) {
            System.out.println("Не могу стартовать сервер");
            e.printStackTrace();
        }
    }
}