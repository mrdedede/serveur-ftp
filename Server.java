import java.net.*;

public class Server {
    public static void main(String[] args) throws Exception {
        try (ServerSocket serv = new ServerSocket(21)) {
            System.out.println("SERVEUR SOCKET ÉCOUTE SUR LE PORT 21");

            while (true) {
                Socket socket = serv.accept();
                System.out.println("CLIENT CONNECTÉ SUR LE PORT " + socket.getPort());

                UserHandler curUser = new UserHandler(socket);
                new Thread(() -> curUser.handleClient()).start();
            }
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

}
