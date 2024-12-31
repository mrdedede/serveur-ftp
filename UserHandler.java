import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class UserHandler {
    Socket userSocket;

    UserHandler(Socket userSocket) {
        this.userSocket = userSocket;
    }

    public void handleClient() {
        try (
            InputStream in = this.userSocket.getInputStream();
            OutputStream out = this.userSocket.getOutputStream();
            Scanner scanner = new Scanner(in)
        ) {
            out.write("220 SERVICE PRÊT\r\n".getBytes());

            while (true) {
                if (!scanner.hasNextLine()) {
                    break;
                }

                String commandLine = scanner.nextLine();
                System.out.println("Reçu: " + commandLine);

                String[] commandParts = commandLine.split(" ", 2);

                switch (commandParts[0]) {
                    case "USER":
                        out.write("331 USER NAME OK, INSEREZ LE MOT DE PASSE\r\n".getBytes());
                        break;

                    case "PASS":
                        out.write("230 USER LOGGED IN\r\n".getBytes());
                        break;

                    case "RETR":
                        this.handleRETRCommand(commandParts[1], in, out);
                        break;

                    case "QUIT":
                        out.write("221 GOODBYE\r\n".getBytes());
                        userSocket.close();
                        System.out.println("CLIENT DÉCONNECTÉ");
                        return;

                    default:
                        out.write(("200 COMMAND OK: " + commandParts[0] + "\r\n").getBytes());
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur avec le client: " + e.getMessage());
        }
    }

    private void handleRETRCommand(String file, InputStream in, OutputStream out) throws IOException {
        File f = new File("./" + file);

        if (!f.exists()) {
            out.write("550 LE FICHIER N'EXISTE PAS\r\n".getBytes());
        } else {
            out.write("150 FICHIER OK - OUVRIR CONNEXION EN MODE DATA\r\n".getBytes());
            try {
                BufferedInputStream readFileStream = new BufferedInputStream(new FileInputStream(f));
                BufferedOutputStream sendFileStream = new BufferedOutputStream(this.userSocket.getOutputStream());

                byte[] buffer = new byte[1024];
                int i = 0;
                while((i = readFileStream.read(buffer, 0, buffer.length)) != -1) {
                    sendFileStream.write(buffer, 0, i);
                }
                readFileStream.close();
                sendFileStream.close();
                out.write("226 TRANSFERT CONCLU - FERMER CONNEXION EN MODE DATAa\r\n".getBytes());
            } catch (Exception e) {
                out.write("451 TRANSFERT AVORTÉE - FERMER CONNEXION EN MODE DATA\r\n".getBytes());
            }
        }

    }

}